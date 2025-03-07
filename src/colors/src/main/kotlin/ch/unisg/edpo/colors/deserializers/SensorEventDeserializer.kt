package ch.unisg.edpo.colors.deserializers

import ch.unisg.edpo.proto.events.sensors.v1.SensorEvent
import org.apache.kafka.common.serialization.Deserializer

class SensorEventDeserializer : Deserializer<SensorEvent> {
    override fun deserialize(topic: String?, data: ByteArray?): SensorEvent {
        try {
            return SensorEvent.parseFrom(data)
        } catch (e: Exception) {
            throw RuntimeException("Error deserializing sensor event", e)
        }
    }
}
