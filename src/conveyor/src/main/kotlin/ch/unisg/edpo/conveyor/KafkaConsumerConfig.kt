package ch.unisg.edpo.conveyor

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.KafkaListenerErrorHandler


@Configuration
@EnableKafka
class KafkaConsumerConfig {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Value("\${spring.kafka.bootstrap-servers: localhost:9092}")
    private val bootstrapServers: String? = null

    //    @Bean
//    fun sensorEventConsumerFactory(): ConsumerFactory<String, SensorEvent> {
//        val config: MutableMap<String, Any> = HashMap()
//
//        config[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers!!
//        config[ConsumerConfig.GROUP_ID_CONFIG] = "sensor-reader"
//        config[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
//        config[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = SensorEventDeserializer::class.java
//
//        return DefaultKafkaConsumerFactory(config)
//    }
//
//    @Bean
//    fun kafkaListenerSensorEventFactory(): ConcurrentKafkaListenerContainerFactory<String, SensorEvent> {
//        val containerFactory = ConcurrentKafkaListenerContainerFactory<String, SensorEvent>()
//        containerFactory.consumerFactory = sensorEventConsumerFactory()
//        containerFactory.isBatchListener = true
//        return containerFactory
//    }
//
    @Bean
    fun fuckyou(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val config: MutableMap<String, Any> = HashMap()

        config[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers!!
        config[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        config[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        config[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java

        val fucktory = DefaultKafkaConsumerFactory(config, StringDeserializer(), StringDeserializer())

        val containerFactory = ConcurrentKafkaListenerContainerFactory<String, String>()
        containerFactory.consumerFactory = fucktory
//        containerFactory.isBatchListener = true
        return containerFactory
    }

    @Bean
    fun errorHandler(): KafkaListenerErrorHandler {
        return KafkaListenerErrorHandler { message, exception ->
            logger.error("Error handling message: $message", exception)
        }
    }

    @Bean
    fun stringConsumerFactory(): ConsumerFactory<String, String> {
        val config: MutableMap<String, Any> = HashMap()

        config[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers!!
        config[ConsumerConfig.GROUP_ID_CONFIG] = "sensor-reader"
        config[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        config[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false
        config[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        config[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java

        return DefaultKafkaConsumerFactory(config, StringDeserializer(), StringDeserializer())
    }

    @Bean
    fun kafkaListenerStringFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = stringConsumerFactory()
        factory.isBatchListener = true
        return factory
    }
}
