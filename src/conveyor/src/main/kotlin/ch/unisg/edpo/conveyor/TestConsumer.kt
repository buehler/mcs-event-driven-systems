package ch.unisg.edpo.conveyor

import ch.unisg.edpo.proto.events.sensors.v1.SensorEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class TestConsumer {
    val logger = LoggerFactory.getLogger(this.javaClass)

    @KafkaListener(
        topics = ["\${topics.sensors}"],
        groupId = "sensor-reader",
        containerFactory = "kafkaListenerStringFactory"
    )
    fun consume(message: String) {
        logger.info("Consumed message: $message")
    }
}
