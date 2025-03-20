package ch.unisg.edpo.picker.listeners

import ch.unisg.edpo.picker.Robot
import ch.unisg.edpo.picker.producer.EventProducer
import ch.unisg.edpo.proto.commands.machines.v1.MoveBlockFromShipmentToNfc
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
            "MoveBlockFromShipmentToNfc" -> {
                val cmd = MoveBlockFromShipmentToNfc.parseFrom(payload)
                logger.info("Received command to move block from shipment position {} to nfc reader.", cmd.position)
                robot.moveBlockFromShipmentToNfc(cmd.position)
                eventProducer.sendBlockOnNfc()
            }

            "MoveBlockFromNfcToConveyor" -> {
                logger.info("Received command to move block from nfc reader to conveyor")
                robot.moveBlockFromNfcToConveyor()
                eventProducer.sendBlockOnConveyor()
            }

            "SortBlock" -> {
                val cmd = SortBlock.parseFrom(payload)
                if (cmd.color == BlockColor.BLOCK_COLOR_GREEN) {
                    logger.info("Received command to sort a green block")
                    robot.sortGreenBlock()
                    eventProducer.sendBlockSorted(cmd.color)
                } else {
                    logger.debug("Received command to sort a block with color {}", cmd.color)
                }
            }

            else -> logger.debug("Received command with type $messageType")
        }
    }
}
