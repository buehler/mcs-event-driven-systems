package ch.unisg.edpo.conveyor.listeners

import ch.unisg.edpo.conveyor.ConveyorBelt
import ch.unisg.edpo.conveyor.producer.EventProducer
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

@Service
class CommandListener(private val conveyorBelt: ConveyorBelt, private val eventProducer: EventProducer) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["\${topics.commands}"],
        containerFactory = "kafkaListenerFactory"
    )
    suspend fun listen(@Header("messageType") messageType: String, @Payload payload: ByteArray) {
        when (messageType) {
            "ConveyorMoveBlock" -> {
                logger.info("Received command to move block")
                conveyorBelt.moveBlock()
                eventProducer.sendBlockMoved()
            }

            else -> logger.debug("Received command with type $messageType")
        }
    }
}
