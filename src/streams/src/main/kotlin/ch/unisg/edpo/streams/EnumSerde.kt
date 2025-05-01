package ch.unisg.edpo.streams

import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serializer

class EnumSerde<T : Enum<T>>(private val enumClass: Class<T>) : Serde<T> {
    override fun serializer(): Serializer<T> = Serializer { _, data ->
        data?.name?.toByteArray(Charsets.UTF_8)
    }

    override fun deserializer(): Deserializer<T> = Deserializer { _, data ->
        enumClass.enumConstants.firstOrNull { it.name == String(data, Charsets.UTF_8) }
    }
}