package ch.unisg.edpo.picker

import ch.unisg.edpo.proto.models.v1.PickupPosition
import khttp.get
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service

@Service
class Robot(private val env: Environment) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${robot.server}")
    private val server = ""

    @Value("\${robot.speed}")
    private val speed = 0

    @Value("\${robot.exec.to-conveyor.from-nfc}")
    private val nfcToConveyorFlow = ""

    @Value("\${robot.exec.to-inventory.from-nfc}")
    private val nfcToInventoryFlow = ""

    suspend fun moveBlockFromShipmentToNfc(shipmentPosition: PickupPosition) {
        val flow = getShipmentPositionFlow(shipmentPosition)
        logger.info("Moving block from shipment position $shipmentPosition to NFC reader")
        executeCommand("run_flow?flow=$flow&speed=$speed&loop=0")
    }

    suspend fun moveBlockFromNfcToConveyor() {
        logger.info("Moving block from NFC reader to conveyor")
        executeCommand("run_flow?flow=$nfcToConveyorFlow&speed=$speed&loop=0")
    }

    suspend fun sortGreenBlock() {
        logger.info("Move the green block from the NFC reader to the inventory")
        executeCommand("run_flow?flow=$nfcToInventoryFlow&speed=$speed&loop=0")
    }

    private suspend fun executeCommand(command: String) {
        val response = get("http://$server/$command")
        if (response.statusCode > 299) {
            logger.error("Failed to execute command $command: ${response.statusCode} - ${response.text}")
        } else {
            logger.info("Executed command $command")
        }
    }

    private fun getShipmentPositionFlow(shipmentPosition: PickupPosition): String {
        val posName = when (shipmentPosition) {
            PickupPosition.PICKUP_POSITION_TOP_LEFT -> "top-left"
            PickupPosition.PICKUP_POSITION_TOP_MIDDLE -> "top-middle"
            PickupPosition.PICKUP_POSITION_TOP_RIGHT -> "top-right"
            PickupPosition.PICKUP_POSITION_MIDDLE_LEFT -> "middle-left"
            PickupPosition.PICKUP_POSITION_MIDDLE_MIDDLE -> "middle-middle"
            PickupPosition.PICKUP_POSITION_MIDDLE_RIGHT -> "middle-right"
            PickupPosition.PICKUP_POSITION_BOTTOM_LEFT -> "bottom-left"
            PickupPosition.PICKUP_POSITION_BOTTOM_MIDDLE -> "bottom-middle"
            PickupPosition.PICKUP_POSITION_BOTTOM_RIGHT -> "bottom-right"
            else -> throw IllegalArgumentException("Unknown shipment position type: ${shipmentPosition.javaClass.name}")
        }
        return env.getProperty("robot.exec.to-nfc.from-shipment-$posName")
            ?: throw IllegalArgumentException("Unknown shipment position: $posName")
    }
}