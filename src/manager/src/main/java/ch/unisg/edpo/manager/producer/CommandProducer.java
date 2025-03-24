package ch.unisg.edpo.manager.producer;

import com.google.protobuf.Message;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class CommandProducer {

    private final Logger logger = LoggerFactory.getLogger(EventProducer.class);

    @Value("${topics.commands}")
    private String commandTopic;

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public CommandProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendCommand(Message message, String messageType) {
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(commandTopic, message.toByteArray());
        record.headers().add("messageType", messageType.getBytes());

        kafkaTemplate.send(record);
        logger.info("Successfully sent message '{}' to topic '{}'", messageType, commandTopic);
    }
}