package ch.unisg.edpo.manager.producer;

import com.google.protobuf.Message;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventProducer {

    private final Logger logger = LoggerFactory.getLogger(EventProducer.class);

    @Value("${topics.events}")
    private String eventTopic; // Removed the `final` modifier to allow Spring to inject the value.

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    // Constructor injection for KafkaTemplate
    public EventProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(Message message, String messageType) {
        if (eventTopic == null || eventTopic.isEmpty()) {
            logger.error("Event topic is not configured. Please check the 'topics.events' configuration property.");
            throw new IllegalStateException("Event topic configuration is missing.");
        }

        // Create a Kafka producer record with the Protobuf message payload
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(eventTopic, message.toByteArray());

        // Add the message type to the Kafka headers
        record.headers().add("messageType", messageType.getBytes());

        // Send the record using KafkaTemplate
        kafkaTemplate.send(record);

        logger.info("Successfully sent message '{}' to topic '{}'", messageType, eventTopic);
    }
}