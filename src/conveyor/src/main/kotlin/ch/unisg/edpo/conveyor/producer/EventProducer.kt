package ch.unisg.edpo.conveyor.producer

import ch.unisg.edpo.proto.events.machines.v1.ConveyorBlockMoved
import kotlinx.coroutines.future.await
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class EventProducer(private val kafkaTemplate: KafkaTemplate<String, ByteArray>) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${topics.events}")
    private val eventTopic = ""

    suspend fun sendBlockMoved() {
        val event = ConveyorBlockMoved.newBuilder().build()
        val record = ProducerRecord<String, ByteArray>(eventTopic, event.toByteArray())
        with(record) {
            headers().add("messageType", "ConveyorBlockMoved".toByteArray())
        }

        kafkaTemplate.send(record).await()
        logger.info("Sent conveyor block moved event")
    }
}
