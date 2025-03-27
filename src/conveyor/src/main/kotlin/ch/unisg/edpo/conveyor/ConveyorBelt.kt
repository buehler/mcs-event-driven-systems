package ch.unisg.edpo.conveyor

import khttp.get
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

enum class ConveyorDirection(val value: Int) {
    LEFT(1), RIGHT(0)
}

@Service
class ConveyorBelt {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${conveyor.server}")
    private val server: String = ""

    @Value("\${conveyor.min-speed}")
    private val minSpeed = 0

    @Value("\${conveyor.max-speed}")
    private val maxSpeed = 0

    @Value("\${conveyor.distance}")
    private val distance = 0

    var speed = 100
        set(value) {
            val calcSpeed = minSpeed + (((value * -1) + maxSpeed) / 6)
            field = when {
                calcSpeed < minSpeed -> minSpeed
                calcSpeed > maxSpeed -> maxSpeed
                else -> calcSpeed
            }
        }

    suspend fun moveBlock() {
        logger.info("Moving block on conveyor from picker to color sensor robot")

        val direction = ConveyorDirection.LEFT
        logger.info("Starting conveyor on server $server in direction ${direction.name}")

        val response =
            get("http://$server/move_conveyor?speed-mm-s=$speed&distance-mm=$distance&direction=${direction.value}")
        if (response.statusCode > 299) {
            logger.error("Failed to start conveyor on server $server: ${response.statusCode} - ${response.text}")
        } else {
            logger.info("Conveyor started successfully on server $server")
        }
    }
}
