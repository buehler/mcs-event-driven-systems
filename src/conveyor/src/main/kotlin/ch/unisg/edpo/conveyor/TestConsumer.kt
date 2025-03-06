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
        containerFactory = "kafkaListenerSensorEventFactory"
    )
    fun consumeMessage(message: SensorEvent) {
        logger.info("**** -> Consumed message -> {}", message)
    }
}
