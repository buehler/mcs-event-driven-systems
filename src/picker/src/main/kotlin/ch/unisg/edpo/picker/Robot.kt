package ch.unisg.edpo.picker

import khttp.get
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service

@Service
class Robot(private val env: Environment) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${robot.server}")
    private val server = ""

    private fun getExecutionFile(): String {
        return env.getProperty("")
    }

//    @Value("\${robot.exec.to-nfc.from-shipment-top-left}")
//    private val asdf = ""
//
//    @Value("\${robot.exec.to-nfc.from-shipment-top-center}")
//    private val asdf = ""
//
//    @Value("\${robot.exec.to-nfc.from-shipment-top-right}")
//    private val asdf = ""
//
//    @Value("\${robot.exec.to-nfc.from-shipment-middle-left}")
//    private val asdf = ""
//
//    @Value("\${robot.exec.to-nfc.from-shipment-middle-center}")
//    private val asdf = ""
//
//    @Value("\${robot.exec.to-nfc.from-shipment-middle-right}")
//    private val asdf = ""
//
//    @Value("\${robot.exec.to-nfc.from-shipment-bottom-left}")
//    private val asdf = ""
//
//    @Value("\${robot.exec.to-nfc.from-shipment-bottom-center}")
//    private val asdf = ""
//
//    @Value("\${robot.exec.to-nfc.from-shipment-bottom-right}")
//    private val asdf = ""
//
//    @Value("\${robot.exec.to-conveyor.from-nfc}")
//    private val asdf = ""
//
//    @Value("\${robot.exec.to-inventory.from-nfc}")
//    private val asdf = ""

//    suspend fun moveBlock() {
//        logger.info("Moving block on conveyor from picker to color sensor robot")
//
//        startConveyor()
//        delay(timeAtBaseSpeed)
//        stopConveyor()
//    }
//
//    private fun startConveyor() {
//        val direction = ConveyorDirection.LEFT
//        logger.info("Starting conveyor on server $server in direction ${direction.name}")
//
//        val response = get("http://$server/conveyor?speed=1&direction=${direction.value}")
//        if (response.statusCode > 299) {
//            logger.error("Failed to start conveyor on server $server: ${response.statusCode} - ${response.text}")
//        } else {
//            logger.info("Conveyor started successfully on server $server")
//        }
//    }
//
//    private fun stopConveyor() {
//        logger.info("Stopping conveyor on server $server")
//
//        val response = get("http://$server/conveyor?speed=0&direction=1")
//        if (response.statusCode > 299) {
//            logger.error("Failed to stop conveyor on server $server: ${response.statusCode} - ${response.text}")
//        } else {
//            logger.info("Conveyor stopped successfully on server $server")
//        }
//    }
}