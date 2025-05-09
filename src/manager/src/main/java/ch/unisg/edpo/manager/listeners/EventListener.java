package ch.unisg.edpo.manager.listeners;
import ch.unisg.edpo.manager.testing.TestingStatusService;

import ch.unisg.edpo.proto.events.machines.v1.BlockPositionedOnNfc;
import ch.unisg.edpo.proto.events.machines.v1.BlockSorted;
import ch.unisg.edpo.proto.events.sensors.v1.NFCDistDetected;
import ch.unisg.edpo.proto.events.sensors.v1.NFCObjectDetected;
import org.camunda.bpm.engine.RuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ch.unisg.edpo.proto.events.sensors.v1.ColorDetected;
import ch.unisg.edpo.proto.models.v1.BlockColor;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventListener {

    private final Logger logger = LoggerFactory.getLogger(EventListener.class);
    private final RuntimeService runtimeService; // Camunda RuntimeService dependency for managing processes
    private final TestingStatusService testingStatusService;


    // Constructor to inject dependencies
    public EventListener(RuntimeService runtimeService, TestingStatusService testingStatusService) {
        this.runtimeService = runtimeService;
        this.testingStatusService =testingStatusService;
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

            case "NFCObjectRemoved":
                handleNFCObjectRemoved(payload);
                break;

            case "NFCDistDetected":
            handleNFCDistDetected(payload);
            break;

            case "NFCDistRemoved":
                handleNFCDistRemoved(payload);
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

            case "BlockSorted":
                handleBlockSorted(payload);
                break;
            // all following cases will only be handled when in testing mode. Otherwise this are unhandled events
            case "ShipmentProcessed":
                handleShipmentProcessed(payload);
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
     * Handles the 'NFCObjectRemoved' message.
     * Correlates the message NFCObjectRemoved in Camunda.
     */
    private void handleNFCObjectRemoved(byte[] payload) {
        try {
            logger.info("Handling 'NFCObjectRemoved' event.");
            runtimeService.createMessageCorrelation("NFCObjectRemoved").correlateAll();
            logger.info("Message `NFCObjectRemoved` successfully correlated in Camunda.");
        } catch (Exception e) {
            logger.error("Error while processing `NFCObjectRemoved` event:", e);
        }
    }

    /**
     * Handles the 'NFCDistDetected' message.
     * Correlates the message NFCDistDetected in Camunda.
     */
    private void handleNFCDistDetected(byte[] payload) {
        try {
            logger.info("Handling 'NFCDistDetected' event.");
            runtimeService.createMessageCorrelation("NFCDistDetected").correlateAll();
            logger.info("Message `NFCDistDetected` successfully correlated in Camunda.");
        } catch (Exception e) {
            logger.error("Error while processing `NFCDistDetected` event:", e);
        }
    }

    /**
     * Handles the 'NFCDistRemoved' message.
     * Correlates the message NFCDistRemoved in Camunda.
     */
    private void handleNFCDistRemoved(byte[] payload) {
        try {
            logger.info("Handling 'NFCDistRemoved' event.");
            runtimeService.createMessageCorrelation("NFCDistRemoved").correlateAll();
            logger.info("Message `NFCDistRemoved` successfully correlated in Camunda.");
        } catch (Exception e) {
            logger.error("Error while processing `NFCDistRemoved` event:", e);
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

    /**
     * Handles the 'BlockSorted' message.
     * Correlates the event in Camunda and logs the process variable for the block's color.
     */
    private void handleBlockSorted(byte[] payload) {
        try {
            logger.info("Handling 'BlockSorted' event...");

            BlockSorted blockSorted = BlockSorted.parseFrom(payload);

            String sortedBlockColor = blockSorted.getColor().toString();
            logger.info("Block color from 'BlockSorted' event: {}", sortedBlockColor);

            runtimeService.createMessageCorrelation("BlockSorted").correlateAll();

        } catch (Exception e) {
            logger.error("Error while processing 'BlockSorted' message:", e);
        }
    }

    /**
     * All following event handler are only active when in testing mode!!
     */

    /**
     * Handles the 'ShipmentProcessed' event when in testing mode.
     */
    private void handleShipmentProcessed(byte[] payload) {
        try {
            // Check if testing mode is active
            if (testingStatusService.isTestingRunning()) {
                logger.info("Testing: Processing ShipmentProcessed event");

                // Get all blocks from the testing service
                List<TestingStatusService.BlockStatus> blocks = testingStatusService.getBlocks();

                // Check if all blocks have checkedForNFC = 1 and all non-green blocks have colorChecked = 1
                boolean allBlocksProcessed = blocks.stream().allMatch(block ->
                        block.getCheckedForNFC() == 1 &&
                                (block.getColor().equalsIgnoreCase("GREEN") || block.getColorChecked() == 1)
                );

                if (allBlocksProcessed) {
                    logger.info("Testing: All blocks have been successfully processed");
                    // Stop testing mode since all conditions are met
                    testingStatusService.stopTesting();
                    logger.info("Testing: Test run completed successfully");
                } else {
                    // Log which blocks haven't been fully processed
                    List<TestingStatusService.BlockStatus> unprocessedBlocks = blocks.stream()
                            .filter(block ->
                                    block.getCheckedForNFC() == 0 ||
                                            (!block.getColor().equalsIgnoreCase("GREEN") && block.getColorChecked() == 0)
                            )
                            .collect(Collectors.toList());

                    logger.warn("Testing: Shipment processed but {} blocks were not fully handled",
                            unprocessedBlocks.size());

                    for (TestingStatusService.BlockStatus block : unprocessedBlocks) {
                        logger.warn("Testing: Block {} ({}): NFC checked={}, Color checked={}, Requires color check: {}",
                                block.getBlockNumber(),
                                block.getColor(),
                                block.getCheckedForNFC(),
                                block.getColorChecked(),
                                !block.getColor().equalsIgnoreCase("GREEN"));
                    }
                }
            } else {
                logger.warn("Unhandled command type: ShipmentProcessed");
            }
        } catch (Exception e) {
            logger.error("Error handling ShipmentProcessed event: {}", e.getMessage(), e);
        }
    }
}