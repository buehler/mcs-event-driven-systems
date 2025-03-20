package ch.unisg.edpo.manager.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class CommandListener {
    private final Logger logger = LoggerFactory.getLogger(CommandListener.class);

    @KafkaListener(
            topics = "${topics.commands}",
            containerFactory = "kafkaListenerFactory"
    )
    public void listen(
            @Header("messageType") String messageType,
            @Payload byte[] payload) {
        logger.info("Received command message with type: {} and payload {}", messageType, payload);
        // here you can use switch case etc to handle different types of messages
    }
}
