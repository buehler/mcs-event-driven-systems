package ch.unisg.edpo.streams

import ch.unisg.edpo.proto.commands.machines.v1.ConveyorMoveBlock
import ch.unisg.edpo.proto.events.machines.v1.ConveyorBlockMoved
import com.google.protobuf.GeneratedMessage
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.processor.api.FixedKeyProcessor
import org.apache.kafka.streams.processor.api.FixedKeyProcessorContext
import org.apache.kafka.streams.processor.api.FixedKeyProcessorSupplier
import org.apache.kafka.streams.processor.api.FixedKeyRecord
import org.apache.kafka.streams.state.KeyValueStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component


@Component
class MonitoringStream {
    @Value("\${topics.commands}")
    private val commandsTopic = ""

    @Value("\${topics.events}")
    private val eventsTopic = ""

    @Autowired
    fun monitoringTable(sb: StreamsBuilder): KTable<String, AvgAggregator> {
        val cmds = sb
            .stream(commandsTopic, Consumed.with(Serdes.String(), GeneratedMessageSerdes()))
            .filter { _, v -> v is ConveyorMoveBlock }
            .selectKey { _, v -> "avg-conveyor-moving" }

        val evts = sb
            .stream(eventsTopic, Consumed.with(Serdes.String(), GeneratedMessageSerdes()))
            .filter { _, v -> v is ConveyorBlockMoved }
            .selectKey { _, v -> "avg-conveyor-moving" }

        val tbl = cmds
            .merge(evts)
            .processValues(AvgTimeProcessor())
            .filter { _, v -> v != null }
            .groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
            .aggregate(
                { -> AvgAggregator() },
                { _, v, agg -> agg.add(v); agg },
                Materialized.`as`<String, AvgAggregator, KeyValueStore<Bytes, ByteArray>>("avg-conveyor-moving-table")
                    .withKeySerde(Serdes.String())
                    .withValueSerde(AvgAggregatorSerdes())
            )

        return tbl
    }
}

@Serializable
data class AvgAggregator(var sum: Long = 0L, var count: Long = 0L) {
    fun add(value: Long) {
        sum += value
        count++
    }

    fun get(): Long = if (count == 0L) 0 else sum / count
}

private class AvgAggregatorSerializer : Serializer<AvgAggregator> {
    override fun serialize(topic: String?, data: AvgAggregator?): ByteArray? {
        if (data == null) {
            return null
        }
        val json = Json.encodeToString(data)
        return Serdes.String().serializer().serialize(topic, json)
    }
}

private class AvgAggregatorDeserializer : Deserializer<AvgAggregator> {
    override fun deserialize(topic: String?, data: ByteArray?): AvgAggregator? {
        if (data == null) {
            return null
        }
        val json = Serdes.String().deserializer().deserialize(topic, data)
        return try {
            Json.decodeFromString(json)
        } catch (_: Exception) {
            null
        }
    }
}

private class AvgAggregatorSerdes : Serdes.WrapperSerde<AvgAggregator> {
    constructor() : super(AvgAggregatorSerializer(), AvgAggregatorDeserializer())
}

private class AvgTimeProcessor : FixedKeyProcessorSupplier<String, GeneratedMessage, Long>,
    FixedKeyProcessor<String, GeneratedMessage, Long> {
    private lateinit var context: FixedKeyProcessorContext<String, Long>
    private var lastCmdReceived: Long? = null

    override fun init(context: FixedKeyProcessorContext<String, Long>) {
        this.context = context
    }

    override fun process(record: FixedKeyRecord<String, GeneratedMessage>) {
        when (record.value()) {
            is ConveyorMoveBlock -> {
                lastCmdReceived = record.timestamp()
                context.forward(record.withValue(null))
            }

            is ConveyorBlockMoved -> {
                if (lastCmdReceived == null) {
                    context.forward(record.withValue(null))
                    return
                } else {
                    val timeDiff = record.timestamp() - lastCmdReceived!!
                    context.forward(record.withValue(timeDiff))
                    lastCmdReceived = null
                }
            }
        }
    }

    override fun get(): FixedKeyProcessor<String?, GeneratedMessage?, Long?>? =
        AvgTimeProcessor() as? FixedKeyProcessor<String?, GeneratedMessage?, Long?>
}