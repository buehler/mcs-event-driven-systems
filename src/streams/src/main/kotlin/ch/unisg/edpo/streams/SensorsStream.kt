package ch.unisg.edpo.streams

import ch.unisg.edpo.proto.events.sensors.v1.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.protobuf.GeneratedMessage
import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Branched
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.processor.PunctuationType
import org.apache.kafka.streams.processor.api.FixedKeyProcessor
import org.apache.kafka.streams.processor.api.FixedKeyProcessorContext
import org.apache.kafka.streams.processor.api.FixedKeyProcessorSupplier
import org.apache.kafka.streams.processor.api.FixedKeyRecord
import org.apache.kafka.streams.state.StoreBuilder
import org.apache.kafka.streams.state.Stores
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration


@Component
class SensorsStream {
    @Value("\${topics.sensors}")
    private val sensorsTopic = ""

    @Value("\${topics.events}")
    private val eventsTopic = ""

    private val allowedCombinations = arrayOf(
        "HumanWS/rotary/KVx",
        "Conveyor/nfc/22Mp",
        "HumanWS/distance_IR_short/TG2",
        "Conveyor/distance_IR_short/TFu",
        "Conveyor/distance_IR_short/2a7C",
        "HumanWS/rgb_button/24dY",
    )

    @Autowired
    fun sensorStream(sb: StreamsBuilder): KStream<String, GeneratedMessage> {
        val stream = sb
            .stream(sensorsTopic, Consumed.with(Serdes.String(), SensorEvent.SerDer()))
            .filter { _, v -> v != null }
            .filter { _, v -> v.sensorAddress in allowedCombinations }
            .mapValues { v -> v!! }
            .processValues(BusinessEventProcessor())
            .filter { _, v -> v != null }
            .split()
            .branch(
                { _, v -> v is NFCObjectRemoved || v is NFCObjectDetected },
                Branched.withFunction { s -> s.processValues(DebounceProcessor()) })
            .defaultBranch()
            .values
            .reduce { acc, stream -> acc.merge(stream) }

        stream.to(eventsTopic, Produced.with(Serdes.String(), GeneratedMessageSerdes()))

        return stream
    }
}

private open class MqttSensorEvent(
    val json: Map<String, Any?>
) {
    val type get() = json["type"] as String
    val uid get() = json["UID"] as String
    val location get() = json["location"] as String?

    val sensorAddress get() = "${location}/${type}/${uid}"
}

private sealed class SensorEvent(json: Map<String, Any?>) : MqttSensorEvent(json) {
    companion object {
        fun fromJson(json: Map<String, Any?>): SensorEvent? {
            return when (json["type"]) {
                "nfc" -> NFC(json)
                "distance_IR_short" -> Distance(json)
                "rotary" -> Rotary(json)
                "rgb_button" -> Button(json)
                else -> null
            }
        }
    }

    class NFC(json: Map<String, Any?>) : SensorEvent(json) {
        val nfcTagId get() = json["ID"] as String?
    }

    class Distance(json: Map<String, Any?>) : SensorEvent(json) {
        val distance get() = json["distance"] as Float
    }

    class Rotary(json: Map<String, Any?>) : SensorEvent(json) {
        val position get() = json["position"] as Int
    }

    class Button(json: Map<String, Any?>) : SensorEvent(json) {
        private val pressedState = "PRESSED!"

        val isPressed get() = json["state"] == pressedState
    }

    class SerDe : Serializer<SensorEvent> {
        private val objectMapper = ObjectMapper()

        override fun serialize(topic: String?, data: SensorEvent?): ByteArray? {
            if (data == null) {
                return null
            }
            return Serdes.String().serializer().serialize(topic, objectMapper.writeValueAsString(data.json))
        }
    }

    class Deser : Deserializer<SensorEvent> {
        private val objectMapper = ObjectMapper()

        override fun deserialize(topic: String?, data: ByteArray?): SensorEvent? {
            if (data == null) {
                return null
            }
            val map = objectMapper.readValue(data) as Map<String, Any?>
            return fromJson(map)
        }
    }

    class SerDer : Serdes.WrapperSerde<SensorEvent> {
        constructor() : super(SerDe(), Deser())
    }
}

private class BusinessEventProcessor : FixedKeyProcessorSupplier<String, SensorEvent, GeneratedMessage>,
    FixedKeyProcessor<String, SensorEvent, GeneratedMessage> {
    override fun get(): FixedKeyProcessor<String?, SensorEvent?, GeneratedMessage?>? =
        BusinessEventProcessor() as? FixedKeyProcessor<String?, SensorEvent?, GeneratedMessage?>

    override fun stores(): Set<StoreBuilder<*>?>? = setOf(
        Stores.keyValueStoreBuilder(
            Stores.persistentKeyValueStore("sensor-event-cache"),
            Serdes.String(),
            SensorEvent.SerDer(),
        )
    )

    private val store = HashMap<String, SensorEvent>()
    private lateinit var context: FixedKeyProcessorContext<String?, GeneratedMessage?>

    private val distanceThresholds = mapOf(
        "Conveyor/distance_IR_short/TFu" to 9.2f,
        "Conveyor/distance_IR_short/2a7C" to 9.2f,
        "HumanWS/distance_IR_short/TG2" to 7.0f,
    )

    override fun init(context: FixedKeyProcessorContext<String?, GeneratedMessage?>?) {
        if (context != null) {
            this.context = context
        }
        super.init(context)
    }

    override fun process(record: FixedKeyRecord<String?, SensorEvent?>?) {
        val current = record?.value()
        if (current == null) {
            return
        }

        val transformed = transform(current)
        var newRecord = record.withValue(transformed)
        if (transformed != null) {
            val headers = RecordHeaders().add("messageType", transformed.javaClass.simpleName.toByteArray())
            newRecord = newRecord.withHeaders(headers)
        }
        context.forward(newRecord)
    }

    private fun transform(value: SensorEvent?): GeneratedMessage? {
        if (value == null) {
            return null
        }

        val prev: SensorEvent? = store[value.sensorAddress]
        return when (value) {
            is SensorEvent.NFC -> handleNFC(value, prev as? SensorEvent.NFC)
            is SensorEvent.Distance -> handleDistance(value, prev as? SensorEvent.Distance)
            is SensorEvent.Rotary -> handleRotary(value, prev as? SensorEvent.Rotary)
            is SensorEvent.Button -> handleButton(value, prev as? SensorEvent.Button)
        }
    }

    private fun handleNFC(current: SensorEvent.NFC, previous: SensorEvent.NFC?): GeneratedMessage? {
        if (previous?.nfcTagId == null && current.nfcTagId != null) {
            // NFC tag read
            store[current.sensorAddress] = current
            return NFCObjectDetected.newBuilder().build()
        } else if (previous?.nfcTagId != null && current.nfcTagId == null) {
            // NFC tag removed
            store[current.sensorAddress] = current
            return NFCObjectRemoved.newBuilder().build()
        }
        return null
    }

    private fun handleDistance(current: SensorEvent.Distance, previous: SensorEvent.Distance?): GeneratedMessage? {
        val threshold = distanceThresholds[current.sensorAddress]
        if (threshold == null) {
            return null
        }

        val prevDist = previous?.distance ?: Float.POSITIVE_INFINITY
        store[current.sensorAddress] = current
        when (Triple(prevDist <= threshold, current.distance <= threshold, current.sensorAddress)) {
            Triple(true, false, "Conveyor/distance_IR_short/TFu") -> {
                // prev was inside, current is outside -> left object removed
                return LeftObjectRemoved.newBuilder().build()
            }

            Triple(false, true, "Conveyor/distance_IR_short/TFu") -> {
                // prev was outside, current is inside -> left object added
                return LeftObjectDetected.newBuilder().build()
            }

            Triple(true, false, "Conveyor/distance_IR_short/2a7C") -> {
                // prev was inside, current is outside -> right object removed
                return RightObjectRemoved.newBuilder().build()
            }

            Triple(false, true, "Conveyor/distance_IR_short/2a7C") -> {
                // prev was outside, current is inside -> right object added
                return RightObjectDetected.newBuilder().build()
            }

            Triple(true, false, "HumanWS/distance_IR_short/TG2") -> {
                // prev was inside, current is outside -> nfc object removed
                return NFCObjectRemoved.newBuilder().build()
            }

            Triple(false, true, "HumanWS/distance_IR_short/TG2") -> {
                // prev was outside, current is inside -> nfc object added
                return NFCObjectDetected.newBuilder().build()
            }

            else -> return null
        }
    }

    private fun handleRotary(current: SensorEvent.Rotary, previous: SensorEvent.Rotary?): GeneratedMessage? {
        if (previous == null || current.position != previous.position) {
            // Rotary position changed
            store[current.sensorAddress] = current
            return ConveyorSpeedChanged.newBuilder().setSpeed(current.position).build()
        }
        return null
    }

    private fun handleButton(current: SensorEvent.Button, previous: SensorEvent.Button?): GeneratedMessage? {
        if (current.isPressed && (previous == null || !previous.isPressed)) {
            // Button pressed event
            store[current.sensorAddress] = current
            return AreaClearButtonPressed.newBuilder().build()
        } else if (!current.isPressed && previous?.isPressed == true) {
            // Button released event
            store[current.sensorAddress] = current
            return null
        }
        return null
    }
}

private class DebounceProcessor : FixedKeyProcessorSupplier<String, GeneratedMessage, GeneratedMessage>,
    FixedKeyProcessor<String, GeneratedMessage, GeneratedMessage> {
    private lateinit var context: FixedKeyProcessorContext<String, GeneratedMessage>
    private val store = HashMap<String, Pair<FixedKeyRecord<String, GeneratedMessage>, Long>>()
    private val debounceMs = 1000L  // Debounce window

    override fun init(context: FixedKeyProcessorContext<String, GeneratedMessage>) {
        this.context = context
        context.schedule(Duration.ofMillis(100), PunctuationType.WALL_CLOCK_TIME) { timestamp ->
            store.entries.forEach { entry ->
                val (event, ts) = entry.value
                if (timestamp - ts >= debounceMs) {
                    context.forward(event)
                    store.remove(entry.key)
                }
            }
        }
    }

    override fun process(record: FixedKeyRecord<String, GeneratedMessage>) {
        if (record.value() is NFCObjectDetected) {
            context.forward(record)
            return
        }
        store.put(record.key(), record to context.currentSystemTimeMs())
    }

    override fun get(): FixedKeyProcessor<String?, GeneratedMessage?, GeneratedMessage?>? =
        DebounceProcessor() as? FixedKeyProcessor<String?, GeneratedMessage?, GeneratedMessage?>
}
