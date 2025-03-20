package ch.unisg.edpo.picker.listeners

import ch.unisg.edpo.picker.Robot
import ch.unisg.edpo.picker.producer.EventProducer
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Service

@Service
class CommandListener(private val robot: Robot, private val eventProducer: EventProducer) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["\${topics.commands}"],
        containerFactory = "kafkaListenerFactory"
    )
    suspend fun listen(@Header("messageType") messageType: String) {
        when (messageType) {
            "ConveyorMoveBlock" -> {
                logger.info("Received command to move block")
            }

            else -> logger.debug("Received command with type $messageType")
        }
    }
}
