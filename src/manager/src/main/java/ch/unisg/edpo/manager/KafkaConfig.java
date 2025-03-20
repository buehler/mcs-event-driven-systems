package ch.unisg.edpo.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;

import java.util.ArrayList;

@Configuration
@EnableKafka
class KafkaConfig {
    private final Logger logger = LoggerFactory.getLogger(KafkaConfig.class);
    private final KafkaProperties kafkaProperties;

    public KafkaConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public ConsumerFactory<String, byte[]> kafkaConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, byte[]> kafkaListenerFactory() {
        var containerFactory = new ConcurrentKafkaListenerContainerFactory<String, byte[]>();
        containerFactory.setConsumerFactory(kafkaConsumerFactory());
        containerFactory.setBatchListener(false);
        return containerFactory;
    }

    @Bean
    public KafkaListenerErrorHandler listenerErrorHandler() {
        return (message, exception) -> {
            logger.error("Error handling message: $message", exception);
            return new Object();
        };
    }

    @Bean
    public ProducerFactory<String, byte[]> kafkaProducerFactory() {
        return new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties());
    }

    @Bean
    public KafkaTemplate<String, byte[]> kafkaTemplate() {
        return new KafkaTemplate<>(kafkaProducerFactory());
    }
}
