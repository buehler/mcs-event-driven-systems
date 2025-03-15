package ch.unisg.edpo.conveyor

import khttp.get
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

enum class ConveyorDirection(val value: Int) {
    LEFT(1), RIGHT(-1)
}

@Service
class ConveyorBelt {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${conveyor.server}")
    private val server: String = ""

    @Value("\${conveyor.time-at-base-speed}")
    private val timeAtBaseSpeed = 0L

    suspend fun moveBlock() {
        logger.info("Moving block on conveyor from picker to color sensor robot")

        startConveyor()
        delay(timeAtBaseSpeed)
        stopConveyor()
    }

    private fun startConveyor() {
        val direction = ConveyorDirection.LEFT
        logger.info("Starting conveyor on server $server in direction ${direction.name}")

        val response = get("http://$server/conveyor?speed=1&direction=${direction.value}")
        if (response.statusCode > 299) {
            logger.error("Failed to start conveyor on server $server: ${response.statusCode} - ${response.text}")
        } else {
            logger.info("Conveyor started successfully on server $server")
        }
    }

    private fun stopConveyor() {
        logger.info("Stopping conveyor on server $server")

        val response = get("http://$server/conveyor?speed=0&direction=1")
        if (response.statusCode > 299) {
            logger.error("Failed to stop conveyor on server $server: ${response.statusCode} - ${response.text}")
        } else {
            logger.info("Conveyor stopped successfully on server $server")
        }
    }
}
