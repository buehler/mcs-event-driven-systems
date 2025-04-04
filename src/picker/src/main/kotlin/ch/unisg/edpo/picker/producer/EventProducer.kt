package ch.unisg.edpo.picker.producer

import ch.unisg.edpo.proto.events.machines.v1.BlockPositionedOnConveyor
import ch.unisg.edpo.proto.events.machines.v1.BlockPositionedOnNfc
import ch.unisg.edpo.proto.events.machines.v1.BlockSorted
import ch.unisg.edpo.proto.models.v1.BlockColor
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

    suspend fun sendBlockOnNfc() {
        val event = BlockPositionedOnNfc.newBuilder().build()
        sendEvent(event)
        logger.info("Sent block on nfc event")
    }

    suspend fun sendBlockOnConveyor() {
        val event = BlockPositionedOnConveyor.newBuilder().build()
        sendEvent(event)
        logger.info("Sent block on conveyor event")
    }

    suspend fun sendBlockSorted(color: BlockColor) {
        val event = with(BlockSorted.newBuilder()) {
            this.color = color
            this.build()
        }
        sendEvent(event)
        logger.info("Sent block sorted event")
    }

    private suspend fun <T> sendEvent(event: T)
            where T : com.google.protobuf.Message {
        val record = ProducerRecord<String, ByteArray>(eventTopic, event.toByteArray())
        with(record) {
            headers().add("messageType", event.javaClass.simpleName.toByteArray())
        }

        try {
            kafkaTemplate.send(record).await()
        } catch (e: Exception) {
            logger.error("Failed to send event ${event.javaClass.simpleName}", e)
        }
    }
}
