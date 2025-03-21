package ch.unisg.edpo.manager.listeners;

import ch.unisg.edpo.proto.commands.inventory.v1.ProcessNewShipment;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EventListener {

    private final Logger logger = LoggerFactory.getLogger(EventListener.class);
    private final RuntimeService runtimeService; // Camunda RuntimeService dependency for managing processes

    // Constructor to inject dependencies
    public EventListener(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @KafkaListener(
            topics = "${topics.events}",       // Kafka topic dynamically loaded from application properties
            containerFactory = "kafkaListenerFactory" // Kafka container factory defined in the config
    )
    public void listen(
            @Header("messageType") String messageType, // Extract message type from Kafka header
            @Payload byte[] payload                    // Extract the protobuf payload
    ) {
        logger.info("Received event message with type: {} and payload of size: {}", messageType, payload.length);

        // Route message based on type
        switch (messageType) {
            case "AreaClearButtonPressed":
                handleAreaClearButtonPressed(payload);
                break;


            default:
                logger.warn("Unhandled message type: {}", messageType);
        }
    }

    /**
     * Handles the 'AreaClearButtonPressed' message.
     * Correlates the message to the message intermediate catch event in Camunda.
     */
    private void handleAreaClearButtonPressed(byte[] payload) {
        try {
            logger.info("Handling 'AreaClearButtonPressed' event.");

            // Correlate the Camunda message `AreaClearButtonPressed` to the waiting process instance.
            // If your Kafka payload carries some unique identifiers, you may extract the necessary details here to correlate specifically.
            runtimeService.createMessageCorrelation("AreaClearButtonPressed")
                    .correlateAll(); // Correlate the message to all waiting instances

            logger.info("Message `AreaClearButtonPressed` successfully correlated in Camunda.");

        } catch (Exception e) {
            logger.error("Error while processing `AreaClearButtonPressed` event:", e);
        }
    }


}