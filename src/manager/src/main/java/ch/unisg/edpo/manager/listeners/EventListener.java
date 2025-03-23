package ch.unisg.edpo.manager.listeners;

import org.camunda.bpm.engine.RuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ch.unisg.edpo.proto.events.sensors.v1.ColorDetected;
import ch.unisg.edpo.proto.models.v1.BlockColor;

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

            case "BlockPositionedOnNfc":
                handleBlockPositionedOnNfc(payload);
                break;

            case "NFCObjectDetected":
                handleNFCObjectDetected(payload);
                break;

            case "BlockPositionedOnConveyor":
                handleBlockPositionedOnConveyor(payload);
                break;

            case "RightObjectDetected":
                handleRightObjectDetected(payload);
                break;

            case "ConveyorBlockMoved":
                handleConveyorBlockMoved(payload);
                break;

            case "LeftObjectDetected":
                handleLeftObjectDetected(payload);
                break;

            case "ColorDetected":
                handleColorDetected(payload);
                break;

            case "BlockPositionedOnColorDetector":
                handleBlockPositionedOnColorDetector(payload);
                break;

            case "RightObjectRemoved":
                handleRightObjectRemoved(payload);
                break;

            case "LeftObjectRemoved":
                handleLeftObjectRemoved(payload);
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
            runtimeService.createMessageCorrelation("AreaClearButtonPressed").correlateAll();
            logger.info("Message `AreaClearButtonPressed` successfully correlated in Camunda.");
        } catch (Exception e) {
            logger.error("Error while processing `AreaClearButtonPressed` event:", e);
        }
    }

    /**
     * Handles the 'BlockPositionedOnNfc' message.
     * Correlates the message BlockPositionedOnNfc in Camunda to the waiting process instance.
     */
    private void handleBlockPositionedOnNfc(byte[] payload) {
        try {
            logger.info("Handling 'BlockPositionedOnNfc' event.");
            runtimeService.createMessageCorrelation("BlockPositionedOnNfc").correlateAll();
            logger.info("Message `BlockPositionedOnNfc` successfully correlated in Camunda.");
        } catch (Exception e) {
            logger.error("Error while processing `BlockPositionedOnNfc` event:", e);
        }
    }

    /**
     * Handles the 'BlockPositionedOnConveyor' message.
     * Correlates the event in Camunda to the waiting process instance.
     */
    private void handleBlockPositionedOnConveyor(byte[] payload) {
        try {
            logger.info("Handling 'BlockPositionedOnConveyor' event.");
            runtimeService.createMessageCorrelation("BlockPositionedOnConveyor").correlateAll();
            logger.info("Message `BlockPositionedOnConveyor` successfully correlated in Camunda.");
        } catch (Exception e) {
            logger.error("Error while processing `BlockPositionedOnConveyor` event:", e);
        }
    }

    /**
     * Handles the 'NFCObjectDetected' message.
     * Correlates the message NFCObjectDetected in Camunda.
     */
    private void handleNFCObjectDetected(byte[] payload) {
        try {
            logger.info("Handling 'NFCObjectDetected' event.");
            runtimeService.createMessageCorrelation("NFCObjectDetected").correlateAll();
            logger.info("Message `NFCObjectDetected` successfully correlated in Camunda.");
        } catch (Exception e) {
            logger.error("Error while processing `NFCObjectDetected` event:", e);
        }
    }

    /**
     * Handles the 'RightObjectDetected' message.
     * Correlates the message RightObjectDetected in Camunda.
     */
    private void handleRightObjectDetected(byte[] payload) {
        try {
            logger.info("Handling 'RightObjectDetected' event.");
            runtimeService.createMessageCorrelation("RightObjectDetected").correlateAll();
            logger.info("Message `RightObjectDetected` successfully correlated in Camunda.");
        } catch (Exception e) {
            logger.error("Error while processing `RightObjectDetected` event:", e);
        }
    }

    /**
     * Handles the 'ConveyorBlockMoved' message.
     * Correlates the message ConveyorBlockMoved in Camunda.
     */
    private void handleConveyorBlockMoved(byte[] payload) {
        try {
            logger.info("Handling 'ConveyorBlockMoved' event.");
            runtimeService.createMessageCorrelation("ConveyorBlockMoved").correlateAll();
            logger.info("Message `ConveyorBlockMoved` successfully correlated in Camunda.");
        } catch (Exception e) {
            logger.error("Error while processing `ConveyorBlockMoved` event:", e);
        }
    }

    /**
     * Handles the 'LeftObjectDetected' message.
     * Correlates the message LeftObjectDetected in Camunda.
     */
    private void handleLeftObjectDetected(byte[] payload) {
        try {
            logger.info("Handling 'LeftObjectDetected' event.");
            runtimeService.createMessageCorrelation("LeftObjectDetected").correlateAll();
            logger.info("Message `LeftObjectDetected` successfully correlated in Camunda.");
        } catch (Exception e) {
            logger.error("Error while processing `LeftObjectDetected` event:", e);
        }
    }

    /**
     * Handles the 'RightObjectRemoved' message.
     * Correlates the message 'RightObjectRemoved' in Camunda.
     */
    private void handleRightObjectRemoved(byte[] payload) {
        try {
            logger.info("Handling 'RightObjectRemoved' event.");
            runtimeService.createMessageCorrelation("RightObjectRemoved").correlateAll();
            logger.info("Message `RightObjectRemoved` successfully correlated in Camunda.");
        } catch (Exception e) {
            logger.error("Error while processing `RightObjectRemoved` event:", e);
        }
    }

    /**
     * Handles the 'LeftObjectRemoved' message.
     * Correlates the message 'LeftObjectRemoved' in Camunda.
     */
    private void handleLeftObjectRemoved(byte[] payload) {
        try {
            logger.info("Handling 'LeftObjectRemoved' event.");
            runtimeService.createMessageCorrelation("LeftObjectRemoved").correlateAll();
            logger.info("Message `LeftObjectRemoved` successfully correlated in Camunda.");
        } catch (Exception e) {
            logger.error("Error while processing `LeftObjectRemoved` event:", e);
        }
    }

    /**
     * Handles the 'ColorDetected' message.
     * Correlates the message 'ColorDetected' in Camunda and sets the process variable `currentBlockColor`.
     */
    private void handleColorDetected(byte[] payload) {
        try {
            logger.info("Handling 'ColorDetected' event...");

            // Deserialize the payload into a Protobuf message
            ColorDetected colorDetected =
                    ColorDetected.parseFrom(payload);

            // Extract the color enum value as a string
            BlockColor blockColor = colorDetected.getColor();
            String blockColorValue = blockColor.name(); // E.g., "BLOCK_COLOR_RED"

            logger.info("Detected block color from Enum: {}", blockColorValue);

            // Correlate the message with Camunda and set the process variable
            runtimeService.createMessageCorrelation("ColorDetected")
                    .setVariable("currentBlockColor", blockColorValue) // Store raw enum name
                    .correlateAll();

            logger.info("Camunda process variable 'currentBlockColor' successfully set to: {}", blockColorValue);
        } catch (Exception e) {
            logger.error("Error while processing 'ColorDetected' message:", e);
        }
    }
    /**
     * Handles the 'BlockPositionedOnColorDetector' message.
     * Correlates the message 'BlockPositionedOnColorDetector' in Camunda.
     */
    private void handleBlockPositionedOnColorDetector(byte[] payload) {
        try {
            logger.info("Handling 'BlockPositionedOnColorDetector' event.");
            runtimeService.createMessageCorrelation("BlockPositionedOnColorDetector").correlateAll();
            logger.info("Message `BlockPositionedOnColorDetector` successfully correlated in Camunda.");
        } catch (Exception e) {
            logger.error("Error while processing `BlockPositionedOnColorDetector` event:", e);
        }
    }
}