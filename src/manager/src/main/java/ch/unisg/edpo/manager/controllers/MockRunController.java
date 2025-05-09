package ch.unisg.edpo.manager.controllers;

import ch.unisg.edpo.manager.producer.CommandProducer;
import ch.unisg.edpo.manager.producer.EventProducer;
import ch.unisg.edpo.manager.testing.TestingStatusService;
import ch.unisg.edpo.proto.commands.inventory.v1.ProcessNewShipment;
import ch.unisg.edpo.proto.events.sensors.v1.AreaClearButtonPressed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.camunda.bpm.engine.RuntimeService;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
class MockRunController {
    private final Logger logger = LoggerFactory.getLogger(MockRunController.class);
    private final CommandProducer commandProducer;
    private final EventProducer eventProducer;
    private final TestingStatusService testingStatusService;
    private final ObjectMapper objectMapper;
    private final RuntimeService runtimeService;


    public MockRunController(
            CommandProducer commandProducer,
            EventProducer eventProducer,
            TestingStatusService testingStatusService,
            ObjectMapper objectMapper,
            RuntimeService runtimeService) {
        this.commandProducer = commandProducer;
        this.eventProducer = eventProducer;
        this.testingStatusService = testingStatusService;
        this.objectMapper = objectMapper;
        this.runtimeService = runtimeService;

    }

    @PostMapping("/mock/testing")
    public ResponseEntity<String> testing(@RequestBody HappyPathRequest request) {
        try {
            // Start testing mode
            if (!testingStatusService.startTesting()) {
                return ResponseEntity.badRequest().body("Test is already running");
            }

            String shipmentId = request.getShipment_id();
            logger.info("Testing: Processing shipment with ID: {}", shipmentId);

            // Parse the blocks string
            String blocksStr = request.getBlocks();
            logger.info("Testing: Raw blocks string: {}", blocksStr);
            blocksStr = blocksStr.substring(1, blocksStr.length() - 1); // Remove square brackets
            String[] blockEntries = blocksStr.split(", ");

            List<Boolean> blocksForCommand = new ArrayList<>();
            List<TestingStatusService.BlockStatus> blockStatusList = new ArrayList<>();
            int blockNumber = 1;

            // Create a JSON object for the old shipment file format
            ObjectNode shipmentFileJson = objectMapper.createObjectNode();
            shipmentFileJson.put("shipment_id", shipmentId);
            ArrayNode blocksArrayNode = shipmentFileJson.putArray("blocks");

            for (String blockEntry : blockEntries) {
                String[] parts = blockEntry.split(":");
                boolean isInShipment = Boolean.parseBoolean(parts[0]);
                String color = parts[1];

                // Add to the blocks array for the command
                blocksForCommand.add(isInShipment);

                // Add to the JSON blocks array (just the boolean value)
                blocksArrayNode.add(isInShipment);

                if (isInShipment) {
                    blockStatusList.add(new TestingStatusService.BlockStatus(blockNumber++, color));
                }
            }

            // Convert to JSON string for logging
            String shipmentFile = objectMapper.writeValueAsString(shipmentFileJson);
            logger.info("Testing: Generated shipment file:\n{}", shipmentFile);

            // Log the color list
            String colorList = blockStatusList.stream()
                    .map(block -> String.format("block %d;%s;%d;%d",
                            block.getBlockNumber(),
                            block.getColor(),
                            block.getCheckedForNFC(),
                            block.getColorChecked()))
                    .collect(Collectors.joining("\n"));
            logger.info("Testing: Color tracking list:\n{}", colorList);

            // Store blocks in the service
            testingStatusService.setBlocks(blockStatusList);

            // Store timeouts in the TestingStatusService for later use
            Duration defaultTimeout = Duration.parse(request.getDefaultTimeout());
            Duration defaultTimeoutTwo = Duration.parse(request.getDefaultTimeoutTwo());
            int simulatedRobotTime  =   Integer.parseInt(request.getSimulatedRobotTime());
            testingStatusService.setTimeouts(defaultTimeout, defaultTimeoutTwo, simulatedRobotTime);
            // Translate and store the failing point
            String translatedFailingPoint = "none"; // Default value is "none"

            if (request.getFailingPoint() != null) {
                switch (request.getFailingPoint()) {
                    case "grid":
                        translatedFailingPoint = "autoRetryGrid";
                        break;
                    case "conveyor":
                        translatedFailingPoint = "autoRetryConveyor";
                        break;
                    case "green":
                        translatedFailingPoint = "autoRetryNfc";
                        break;
                    case "color":
                        translatedFailingPoint = "autoRetryColor";
                        break;
                    case "sorting":
                        translatedFailingPoint = "autoRetrySorting";
                        break;
                    case "none":
                        translatedFailingPoint = "none";
                        break;
                    default:
                        logger.warn("Unknown failing point '{}', defaulting to 'none'", request.getFailingPoint());
                        translatedFailingPoint = "none";
                }
            }

            testingStatusService.setFailingPoint(translatedFailingPoint);

            // Create and send the ProcessNewShipment command
            ProcessNewShipment command = ProcessNewShipment.newBuilder()
                    .setShipmentId(shipmentId)
                    .addAllBlocks(blocksForCommand)
                    .build();

            // Use a separate thread to send the command to allow immediate response
            Thread testThread = new Thread(() -> {
                try {
                    // Send the command using the existing CommandProducer
                    commandProducer.sendCommand(command, "ProcessNewShipment");
                    logger.info("Testing: Happy path started with shipmentId: {}", shipmentId);
                    Thread.sleep(2000);
                    // Create and send AreaClearButtonPressed event
                    AreaClearButtonPressed event = AreaClearButtonPressed.newBuilder().build();
                    eventProducer.sendEvent(event, "AreaClearButtonPressed");
                    logger.info("Testing: Sent AreaClearButtonPressed event");

                } catch (Exception e) {
                    logger.error("Testing: Error during test execution", e);
                    testingStatusService.stopTesting();
                }
            });
            testThread.setDaemon(true); // Allow JVM to exit even if thread is running
            testThread.start();

            return ResponseEntity.ok("Test started with shipmentId: " + shipmentId);
        } catch (Exception e) {
            logger.error("Testing: Error starting test", e);
            testingStatusService.stopTesting();
            return ResponseEntity.internalServerError().body("Error starting test: " + e.getMessage());
        }
    }

    // Request data class
    public static class HappyPathRequest {
        private String shipment_id;
        private String blocks;
        private String defaultTimeout;
        private String defaultTimeoutTwo;
        private String simulatedRobotTime;
        private String failingPoint;


        // Getters and setters
        public String getShipment_id() { return shipment_id; }
        public void setShipment_id(String shipment_id) { this.shipment_id = shipment_id; }
        public String getBlocks() { return blocks; }
        public void setBlocks(String blocks) { this.blocks = blocks; }
        public String getDefaultTimeout() { return defaultTimeout; }
        public void setDefaultTimeout(String defaultTimeout) { this.defaultTimeout = defaultTimeout; }
        public String getDefaultTimeoutTwo() { return defaultTimeoutTwo; }
        public void setDefaultTimeoutTwo(String defaultTimeoutTwo) { this.defaultTimeoutTwo = defaultTimeoutTwo; }
        public String getSimulatedRobotTime() { return simulatedRobotTime; }
        public void setSimulatedRobotTime(String simulatedRobotTime) { this.simulatedRobotTime = simulatedRobotTime; }
        public String getFailingPoint() {return failingPoint;}
        public void setFailingPoint(String failingPoint) {this.failingPoint = failingPoint;}
    }

    @PostMapping("/mock/endTest")
    public ResponseEntity<String> endTest() {
        logger.info("Ending test and cleaning up resources");

        // 1. Stop the testing status service
        testingStatusService.stopTesting();

        // 2. Find and delete the process instance
        org.camunda.bpm.engine.runtime.ProcessInstance processInstance =
                runtimeService.createProcessInstanceQuery().active().singleResult();

        if (processInstance != null) {
            logger.info("Deleting process instance with ID: {}", processInstance.getId());
            runtimeService.deleteProcessInstance(
                    processInstance.getId(),
                    "Test manually ended via /mock/endTest endpoint"
            );
        } else {
            logger.info("No active process instance found to delete");
        }

        // 3. Return a success response
        ObjectNode response = objectMapper.createObjectNode();
        response.put("status", "success");
        response.put("message", "Test ended and resources cleaned up");

        return ResponseEntity.ok(response.toString());
    }
}