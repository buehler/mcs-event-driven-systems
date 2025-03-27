package ch.unisg.edpo.conveyor.listeners

import ch.unisg.edpo.conveyor.ConveyorBelt
import ch.unisg.edpo.conveyor.producer.EventProducer
import ch.unisg.edpo.proto.events.sensors.v1.ConveyorSpeedChanged
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

@Service
class CommandListener(private val conveyorBelt: ConveyorBelt, private val eventProducer: EventProducer) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["\${topics.commands}", "\${topics.events}"],
        containerFactory = "kafkaListenerFactory"
    )
    suspend fun listen(@Header("messageType") messageType: String, @Payload payload: ByteArray) {
        when (messageType) {
            "ConveyorMoveBlock" -> {
                logger.info("Received command to move block")
                conveyorBelt.moveBlock()
                delay(4000)
                eventProducer.sendBlockMoved()
            }
            "ConveyorSpeedChanged"-> {
                val cmd = ConveyorSpeedChanged.parseFrom(payload)
                logger.info("Received command to change speed to ${cmd.speed}")
                conveyorBelt.speed = cmd.speed
            }

            else -> logger.debug("Received command with type $messageType")
        }
    }
}
