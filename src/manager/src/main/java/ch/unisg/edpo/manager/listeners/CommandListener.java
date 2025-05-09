package ch.unisg.edpo.manager.listeners;

import ch.unisg.edpo.manager.producer.EventProducer;
import ch.unisg.edpo.manager.testing.TestingStatusService;
import ch.unisg.edpo.proto.commands.inventory.v1.ProcessNewShipment;
import ch.unisg.edpo.proto.events.machines.v1.*;
import ch.unisg.edpo.proto.events.sensors.v1.*;
import ch.unisg.edpo.proto.commands.machines.v1.SortBlock;
import ch.unisg.edpo.proto.models.v1.BlockColor;
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
public class CommandListener {

    private final Logger logger = LoggerFactory.getLogger(CommandListener.class);
    private final RuntimeService runtimeService;
    private final TestingStatusService testingStatusService;
    private final EventProducer eventProducer;

    // Constructor to inject dependencies
    public CommandListener(RuntimeService runtimeService,
                           TestingStatusService testingStatusService,
                           EventProducer eventProducer) {
        this.runtimeService = runtimeService;
        this.testingStatusService = testingStatusService;
        this.eventProducer = eventProducer;
    }

    @KafkaListener(
            topics = "${topics.commands}",
            containerFactory = "kafkaListenerFactory"
    )
    public void listen(
            @Header("messageType") String messageType,
            @Payload byte[] payload
    ) {
        logger.info("Received command of type: {}", messageType);

        try {
            switch (messageType) {
                case "ProcessNewShipment":
                    handleProcessNewShipment(payload);
                    break;
                // all following cases will only be handled when in testing mode. Otherwise this are unhandled events
                case "MoveBlockFromShipmentToNfc":
                    handleMoveBlockFromShipmentToNfc(payload);
                    break;
                case "MoveBlockFromNfcToConveyor":
                    handleMoveBlockFromNfcToConveyor(payload);
                    break;
                case "ConveyorMoveBlock":
                    handleConveyorMoveBlock(payload);
                    break;
                case "MoveBlockFromConveyorToColorDetector":
                    handleMoveBlockFromConveyorToColorDetector(payload);
                    break;
                case "SortBlock":
                    handleSortBlock(payload);
                    break;

                // Handle other command types as needed
                default:
                    logger.warn("Unhandled command type: {}", messageType);
            }
        } catch (Exception e) {
            logger.error("Error processing command: {}", e.getMessage(), e);
        }
    }

    /**
     * Handles the 'ProcessNewShipment' command.
     */
    private void handleProcessNewShipment(byte[] payload) {
        try {
            // Deserialize the protobuf payload
            ProcessNewShipment shipmentCommand = ProcessNewShipment.parseFrom(payload);

            // Extract required fields from the payload
            String shipmentId = shipmentCommand.getShipmentId();           // Unique entity identifier
            List<Boolean> blocks = shipmentCommand.getBlocksList();       // Relevant data for the process

            // Check if any active process instances exist for this process definition
            long activeInstanceCount = runtimeService
                    .createProcessInstanceQuery()
                    .processDefinitionKey("block_unloading")  // The process definition key in BPMN
                    .active()                                   // Only look for active instances
                    .count();

            // If there's an active process, log and skip starting a new one
            if (activeInstanceCount > 0) {
                logger.warn("A process instance for 'ProcessNewShipment' is already active. No new instance will be started.");
                return; // Skip starting a new process
            }

            // If no active process exists, use Camunda Spin to serialize data into JSON
            SpinJsonNode blocksJson = Spin.JSON(blocks);

            // Start a new process instance
            runtimeService.startProcessInstanceByMessage(
                    "ProcessNewShipment",             // Message name from your BPMN file's start event
                    shipmentId,                       // Business key for the process
                    Map.of("blocks", blocksJson)      // Process variables to include
            );

            logger.info("Started new process instance for 'ProcessNewShipment', shipmentId: {}, with blocks: {}", shipmentId, blocks);

        } catch (Exception e) {
            logger.error("Error while processing 'ProcessNewShipment' command:", e);
        }    }

    /**
     * All following event handler are only active when in testing mode!!
     */

    /**
     * Handles the 'MoveBlockFromShipmentToNfc' command when in testing mode.
     */
    private void handleMoveBlockFromShipmentToNfc(byte[] payload) {
        try {
            // Check if testing mode is active
            if (testingStatusService.isTestingRunning()) {
                logger.info("Testing: Processing MoveBlockFromShipmentToNfc command");

                // Wait for the simulated robot time
                int robotTimeSeconds = testingStatusService.getSimulatedRobotTime();
                logger.info("Testing: Simulating robot movement for {} seconds", robotTimeSeconds);
                Thread.sleep(robotTimeSeconds * 1000L);

                // Find the first block where checkedForNFC is 0
                TestingStatusService.BlockStatus blockToCheck = testingStatusService.getNextBlockForNFC();

                if (blockToCheck != null) {
                    logger.info("Testing: Found block to check for NFC: Block {} with color {}",
                            blockToCheck.getBlockNumber(), blockToCheck.getColor());

                    // If the block is green, send NFCObjectDetected event
                    if ("green".equalsIgnoreCase(blockToCheck.getColor())) {
                        NFCObjectDetected nfcObjectDetected = NFCObjectDetected.newBuilder().build();
                        eventProducer.sendEvent(nfcObjectDetected, "NFCObjectDetected");
                        logger.info("Testing: Sent NFCObjectDetected event for green block {}",
                                blockToCheck.getBlockNumber());
                    } else {
                        logger.info("Testing: Block {} is {}, not sending NFCObjectDetected",
                                blockToCheck.getBlockNumber(), blockToCheck.getColor());
                    }

                    // Mark the block as checked for NFC
                    blockToCheck.setCheckedForNFC(1);


                    // Check if we should skip sending the NFCDistDetected event
                    if (!testingStatusService.getFailingPoint().equals("autoRetryGrid")) {
                        // Only send the event if failing point is not autoRetryGrid
                        NFCDistDetected nfcDistDetected = NFCDistDetected.newBuilder().build();
                        eventProducer.sendEvent(nfcDistDetected, "NFCDistDetected");
                        logger.info("Testing: Sent NFCDistDetected event");
                    } else {
                        // Log that we're skipping the event
                        logger.info("Testing: Skipping NFCDistDetected event due to autoRetryGrid failing point");
                    }

                    // Always send BlockPositionedOnNfc events
                    BlockPositionedOnNfc blockPositionedOnNfc = BlockPositionedOnNfc.newBuilder().build();
                    eventProducer.sendEvent(blockPositionedOnNfc, "BlockPositionedOnNfc");
                    logger.info("Testing: Sent BlockPositionedOnNfc event");
                } else {
                    logger.info("Testing: No more blocks to check for NFC");
                }
            } else {
                logger.warn("Unhandled command type: MoveBlockFromShipmentToNfc");
            }
        } catch (Exception e) {
            logger.error("Error handling MoveBlockFromShipmentToNfc command: {}", e.getMessage(), e);
        }
    }

    /**
     * Handles the 'MoveBlockFromNfcToConveyor' command when in testing mode.
     */
    private void handleMoveBlockFromNfcToConveyor(byte[] payload) {
        try {
            // Check if testing mode is active
            if (testingStatusService.isTestingRunning()) {
                logger.info("Testing: Processing MoveBlockFromNfcToConveyor command");

                // Wait for the simulated robot time
                int robotTimeSeconds = testingStatusService.getSimulatedRobotTime();
                logger.info("Testing: Simulating robot movement for {} seconds", robotTimeSeconds);
                Thread.sleep(robotTimeSeconds * 1000L);

                // Check if we should skip sending the NFCDistRemoved event
                if (!testingStatusService.getFailingPoint().equals("autoRetryConveyor")) {
                    // Only send the event if failing point is not autoRetryConveyor
                    NFCDistRemoved nfcDistRemoved = NFCDistRemoved.newBuilder().build();
                    eventProducer.sendEvent(nfcDistRemoved, "NFCDistRemoved");
                    logger.info("Testing: Sent NFCDistRemoved event");
                } else {
                    // Log that we're skipping the event
                    logger.info("Testing: Skipping NFCDistRemoved event due to autoRetryConveyor failing point");
                }

                // Always send lockPositionedOnConveyor and RightObjectDetected events
                BlockPositionedOnConveyor blockPositionedOnConveyor = BlockPositionedOnConveyor.newBuilder().build();
                eventProducer.sendEvent(blockPositionedOnConveyor, "BlockPositionedOnConveyor");
                logger.info("Testing: Sent BlockPositionedOnConveyor event");

                RightObjectDetected rightObjectDetected = RightObjectDetected.newBuilder().build();
                eventProducer.sendEvent(rightObjectDetected, "RightObjectDetected");
                logger.info("Testing: Sent RightObjectDetected event");

            } else {
                logger.warn("Unhandled command type: MoveBlockFromNfcToConveyor");
            }
        } catch (Exception e) {
            logger.error("Error handling MoveBlockFromNfcToConveyor command: {}", e.getMessage(), e);
        }
    }

    /**
     * Handles the 'ConveyorMoveBlock' command when in testing mode.
     */
    private void handleConveyorMoveBlock(byte[] payload) {
        try {
            // Check if testing mode is active
            if (testingStatusService.isTestingRunning()) {
                logger.info("Testing: Processing ConveyorMoveBlock command");

                // Wait for the simulated robot time
                int robotTimeSeconds = testingStatusService.getSimulatedRobotTime();
                logger.info("Testing: Simulating conveyor movement for {} seconds", robotTimeSeconds);
                Thread.sleep(robotTimeSeconds * 1000L);

                // Always send RightObjectRemoved, ConveyorBlockMoved and LeftObjectDetected events
                RightObjectRemoved rightObjectRemoved = RightObjectRemoved.newBuilder().build();
                eventProducer.sendEvent(rightObjectRemoved, "RightObjectRemoved");
                logger.info("Testing: Sent RightObjectRemoved event");

                ConveyorBlockMoved conveyorBlockMoved = ConveyorBlockMoved.newBuilder().build();
                eventProducer.sendEvent(conveyorBlockMoved, "ConveyorBlockMoved");
                logger.info("Testing: Sent ConveyorBlockMoved event");

                LeftObjectDetected leftObjectDetected = LeftObjectDetected.newBuilder().build();
                eventProducer.sendEvent(leftObjectDetected, "LeftObjectDetected");
                logger.info("Testing: Sent LeftObjectDetected event");

            } else {
                logger.warn("Unhandled command type: ConveyorBlockMoved");
            }
        } catch (Exception e) {
            logger.error("Error handling ConveyorBlockMoved command: {}", e.getMessage(), e);
        }
    }

    /**
     * Handles the 'MoveBlockFromConveyorToColorDetector' command when in testing mode.
     */
    private void handleMoveBlockFromConveyorToColorDetector(byte[] payload) {
        try {
            // Check if testing mode is active
            if (testingStatusService.isTestingRunning()) {
                logger.info("Testing: Processing MoveBlockFromConveyorToColorDetector command");

                // Wait for the simulated robot time
                int robotTimeSeconds = testingStatusService.getSimulatedRobotTime();
                logger.info("Testing: Simulating robot movement for {} seconds", robotTimeSeconds);
                Thread.sleep(robotTimeSeconds * 1000L);

                // Check if we should skip sending the BlockPositionedOnColorDetector event
                if (!testingStatusService.getFailingPoint().equals("autoRetryColor")) {
                    // Only send the event if failing point is not autoRetryColor
                    BlockPositionedOnColorDetector blockPositionedOnColorDetector = BlockPositionedOnColorDetector.newBuilder().build();
                    eventProducer.sendEvent(blockPositionedOnColorDetector, "BlockPositionedOnColorDetector");
                    logger.info("Testing: Sent BlockPositionedOnColorDetector event");
                } else {
                    // Log that we're skipping the event
                    logger.info("Testing: Skipping BlockPositionedOnColorDetector event due to autoRetryColor failing point");
                }

                // Always send LeftObjectRemoved and ColorDetected events
                LeftObjectRemoved leftObjectRemoved = LeftObjectRemoved.newBuilder().build();
                eventProducer.sendEvent(leftObjectRemoved, "LeftObjectRemoved");
                logger.info("Testing: Sent LeftObjectRemoved event");

                // Find the first block with colorChecked = 0 and color is not green
                TestingStatusService.BlockStatus blockToCheck = testingStatusService.getBlocks().stream()
                        .filter(block -> block.getColorChecked() == 0 && !block.getColor().equalsIgnoreCase("GREEN"))
                        .findFirst()
                        .orElse(null);

                if (blockToCheck != null) {
                    // Convert the color string to BlockColor enum
                    BlockColor detectedColor = BlockColor.valueOf("BLOCK_COLOR_" + blockToCheck.getColor().toUpperCase());

                    // Create and send the ColorDetected event with the block's color
                    ColorDetected colorDetected = ColorDetected.newBuilder()
                            .setColor(detectedColor)
                            .build();
                    eventProducer.sendEvent(colorDetected, "ColorDetected");
                    logger.info("Testing: Sent ColorDetected event with color: {}", detectedColor);

                    // Mark the block as color checked
                    blockToCheck.setColorChecked(1);
                    logger.info("Testing: Marked block {} (color: {}) as color checked",
                            blockToCheck.getBlockNumber(), blockToCheck.getColor());
                } else {
                    logger.warn("Testing: No non-green blocks available with colorChecked = 0");
                }



            } else {
                logger.warn("Unhandled command type: MoveBlockFromConveyorToColorDetector");
            }
        } catch (Exception e) {
            logger.error("Error handling MoveBlockFromConveyorToColorDetector command: {}", e.getMessage(), e);
        }
    }

    /**
     * Handles the 'SortBlock' command when in testing mode.
     */
    private void handleSortBlock(byte[] payload) {
        try {
            // Check if testing mode is active
            if (testingStatusService.isTestingRunning()) {
                logger.info("Testing: Processing SortBlock command");

                // Wait for the simulated robot time
                int robotTimeSeconds = testingStatusService.getSimulatedRobotTime();
                logger.info("Testing: Simulating robot movement for {} seconds", robotTimeSeconds);
                Thread.sleep(robotTimeSeconds * 1000L);

                // Parse the SortBlock command from payload
                SortBlock sortBlockCommand = SortBlock.parseFrom(payload);

                // Get the BlockColor from the command
                BlockColor blockColor = sortBlockCommand.getColor();
                logger.info("Testing: Sorting {} block", sortBlockCommand.getColor());


                // Check if the block color is green
                if (blockColor == BlockColor.BLOCK_COLOR_GREEN) {
                    // Sort green block
                    // send NFCDistRemoved event
                    NFCDistRemoved nfcDistRemoved = NFCDistRemoved.newBuilder().build();
                    eventProducer.sendEvent(nfcDistRemoved, "NFCDistRemoved");
                    logger.info("Testing: Sent NFCDistRemoved event");

                    // send NFCObjectRemoved
                    NFCObjectRemoved nfcObjectRemoved = NFCObjectRemoved.newBuilder().build();
                    eventProducer.sendEvent(nfcObjectRemoved, "NFCObjectRemoved");
                    logger.info("Testing: Sent NFCObjectRemoved event");

                    // Check if we should skip sending the BlockSorted event
                    if (!testingStatusService.getFailingPoint().equals("autoRetryNfc")) {
                        // Only send the event if failing point is not autoRetryNfc
                        BlockSorted blockSorted = BlockSorted.newBuilder()
                                .setColor(blockColor)
                                .build();
                        eventProducer.sendEvent(blockSorted, "BlockSorted");
                        logger.info("Testing: Sent BlockSorted event for {} block", sortBlockCommand.getColor());
                    } else {
                        // Log that we're skipping the event
                        logger.info("Testing: Skipping BlockSorted event due to autoRetryNfc (sort green block) failing point");
                    }

                } else {
                    // sort non-green block
                    // Send BlockSorted event
                    
                    // Check if we should skip sending the BlockSorted event
                    if (!testingStatusService.getFailingPoint().equals("autoRetrySorting")) {
                        // Only send the event if failing point is not autoRetrySorting
                        BlockSorted blockSorted = BlockSorted.newBuilder()
                                .setColor(blockColor)
                                .build();
                        eventProducer.sendEvent(blockSorted, "BlockSorted");
                        logger.info("Testing: Sent BlockSorted event for {} block", sortBlockCommand.getColor());
                    } else {
                        // Log that we're skipping the event
                        logger.info("Testing: Skipping BlockSorted event due to autoRetrySorting failing point");
                    }


                }

            } else {
                logger.warn("Unhandled command type: SortBlock");
            }
        } catch (Exception e) {
            logger.error("Error handling SortBlock command: {}", e.getMessage(), e);
        }
    }

}