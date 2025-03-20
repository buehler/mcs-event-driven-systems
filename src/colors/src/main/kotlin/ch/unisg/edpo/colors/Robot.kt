package ch.unisg.edpo.colors

import ch.unisg.edpo.proto.models.v1.BlockColor
import khttp.get
import khttp.responses.Response
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class Robot {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${robot.server}")
    private val server = ""

    @Value("\${robot.speed}")
    private val speed = 0

    @Value("\${robot.exec.to-colors.from-conveyor}")
    private val conveyorToColorReaderFlow = ""

    @Value("\${robot.exec.to-red-inventory.from-colors}")
    private val colorReaderToRedInventoryFlow = ""

    @Value("\${robot.exec.to-yellow-inventory.from-colors}")
    private val colorReaderToYellowInventoryFlow = ""

    @Value("\${robot.exec.to-blue-inventory.from-colors}")
    private val colorReaderToBlueInventoryFlow = ""

    suspend fun moveFromConveyorToColorReader() {
        logger.info("Moving block from conveyor belt to color reader")
        executeCommand("run_flow?flow=$conveyorToColorReaderFlow&speed=$speed&loop=0")
    }

    suspend fun readColorFromSensor(): BlockColor {
        val data = executeCommand("read_color")
        val colorSensorData = Json.decodeFromString(ColorSensorData.serializer(), data.text)

        return when {
            colorSensorData.red -> BlockColor.BLOCK_COLOR_RED
            colorSensorData.green -> BlockColor.BLOCK_COLOR_YELLOW
            colorSensorData.blue -> BlockColor.BLOCK_COLOR_BLUE
            else -> throw IllegalArgumentException("Unknown color")
        }
    }

    suspend fun sortBlock(color: BlockColor) {
        val flow = when (color) {
            BlockColor.BLOCK_COLOR_RED -> colorReaderToRedInventoryFlow
            BlockColor.BLOCK_COLOR_BLUE -> colorReaderToBlueInventoryFlow
            BlockColor.BLOCK_COLOR_YELLOW -> colorReaderToYellowInventoryFlow
            else -> throw IllegalArgumentException("Unknown color")
        }
        logger.info("Sorting {} block to inventory", color)
        executeCommand("run_flow?flow=$flow&speed=$speed&loop=0")
    }

    private suspend fun executeCommand(command: String): Response {
        val response = get("http://$server/$command")
        if (response.statusCode > 299) {
            logger.error("Failed to execute command $command: ${response.statusCode} - ${response.text}")
        } else {
            logger.info("Executed command $command")
        }

        return response
    }
}

@Serializable
private data class ColorSensorData(val color: BooleanArray) {
    val red = color[0]

    val green = color[1]

    val blue = color[2]
}