package ch.unisg.edpo.streams

import com.google.protobuf.GeneratedMessage
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
}

class GeneratedMessageSerdes : Serdes.WrapperSerde<GeneratedMessage> {
    constructor() : super(GeneratedMessageSerializer(), GeneratedMessageDeserializer())
}
