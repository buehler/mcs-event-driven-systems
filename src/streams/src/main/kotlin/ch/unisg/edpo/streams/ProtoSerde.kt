package ch.unisg.edpo.streams

import ch.unisg.edpo.proto.events.machines.v1.BlockSorted
import com.google.protobuf.GeneratedMessage
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.Serializer

class GeneratedMessageSerializer : Serializer<GeneratedMessage> {
    override fun serialize(topic: String?, data: GeneratedMessage?): ByteArray? = data?.toByteArray()
}

class GeneratedMessageDeserializer : Deserializer<GeneratedMessage> {
    override fun deserialize(topic: String?, data: ByteArray?): GeneratedMessage? {
        TODO()
    }

    override fun deserialize(topic: String?, headers: Headers?, data: ByteArray?): GeneratedMessage? {
        if (headers == null || data == null) {
            return null
        }

        val messageType = headers.lastHeader("messageType")?.value()?.toString(Charsets.UTF_8)
        return when (messageType) {
            "BlockSorted" -> BlockSorted.parseFrom(data)
            else -> null
        }
    }
}

class GeneratedMessageSerdes : Serdes.WrapperSerde<GeneratedMessage> {
    constructor() : super(GeneratedMessageSerializer(), GeneratedMessageDeserializer())
}
