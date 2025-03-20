package ch.unisg.edpo.colors.listeners

import ch.unisg.edpo.colors.Robot
import ch.unisg.edpo.colors.producer.EventProducer
import ch.unisg.edpo.proto.commands.machines.v1.SortBlock
import ch.unisg.edpo.proto.models.v1.BlockColor
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

@Service
class CommandListener(private val robot: Robot, private val eventProducer: EventProducer) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["\${topics.commands}"],
        containerFactory = "kafkaListenerFactory"
    )
    suspend fun listen(@Header("messageType") messageType: String, @Payload payload: ByteArray) {
        when (messageType) {
            "MoveBlockFromConveyorToColorDetector" -> {
                logger.info("Received command to move block from conveyor belt to color detector")
                robot.moveFromConveyorToColorReader()
                eventProducer.sendBlockOnColorDetector()
                val color = robot.readColorFromSensor()
                logger.info("Read color sensor: {}", color)
                eventProducer.sendColorDetected(color)
            }

            "SortBlock" -> {
                val cmd = SortBlock.parseFrom(payload)
                if (cmd.color == BlockColor.BLOCK_COLOR_GREEN) {
                    logger.debug("Received command to sort a block with color {}", cmd.color)
                } else {
                    logger.info("Received command to sort a green block")
                    robot.sortBlock(cmd.color)
                    eventProducer.sendBlockSorted(cmd.color)
                }
            }

            else -> logger.debug("Received command with type $messageType")
        }
    }
}
