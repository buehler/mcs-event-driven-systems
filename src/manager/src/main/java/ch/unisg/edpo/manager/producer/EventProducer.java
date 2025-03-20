package ch.unisg.edpo.manager.producer;

import ch.unisg.edpo.proto.events.machines.v1.ConveyorBlockMoved;
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
    private final String eventTopic = "";

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public EventProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendDemoEvent() {
        var event = ConveyorBlockMoved.newBuilder().build();
        var record = new ProducerRecord<String, byte[]>(eventTopic, event.toByteArray());
        record.headers().add("messageType", "ConveyorBlockMoved".getBytes());
        kafkaTemplate.send(record);
        logger.info("Event demo sent to topic: {}", eventTopic);
    }
}
