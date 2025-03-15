package ch.unisg.edpo.conveyor

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.KafkaListenerErrorHandler


@Configuration
@EnableKafka
class KafkaConfig(private val kafkaProperties: KafkaProperties) {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Bean
    fun kafkaConsumerFactory(): ConsumerFactory<String, ByteArray> =
        DefaultKafkaConsumerFactory(kafkaProperties.buildConsumerProperties())

    @Bean
    fun kafkaListenerFactory(): ConcurrentKafkaListenerContainerFactory<String, ByteArray> {
        val containerFactory = ConcurrentKafkaListenerContainerFactory<String, ByteArray>()
        containerFactory.consumerFactory = kafkaConsumerFactory()
        containerFactory.isBatchListener = false
        return containerFactory
    }

    @Bean
    fun listenerErrorHandler(): KafkaListenerErrorHandler = KafkaListenerErrorHandler { message, exception ->
        logger.error("Error handling message: $message", exception)
    }

    @Bean
    fun kafkaProducerFactory(): ProducerFactory<String, ByteArray> =
        DefaultKafkaProducerFactory(kafkaProperties.buildProducerProperties())

    @Bean
    fun kafkaTemplate() = KafkaTemplate(kafkaProducerFactory())
}
