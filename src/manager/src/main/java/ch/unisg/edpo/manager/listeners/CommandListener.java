package ch.unisg.edpo.manager.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import ch.unisg.edpo.proto.commands.inventory.v1.ProcessNewShipment;

import java.util.List;
import java.util.Map;

@Service
public class CommandListener {

    private final Logger logger = LoggerFactory.getLogger(CommandListener.class);
    private final RuntimeService runtimeService; // Camunda RuntimeService dependency for managing processes

    // Constructor to inject dependencies
    public CommandListener(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @KafkaListener(
            topics = "${topics.commands}",       // Kafka topic dynamically loaded from application properties
            containerFactory = "kafkaListenerFactory" // Kafka container factory defined in the config
    )
    public void listen(
            @Header("messageType") String messageType, // Extract message type from Kafka header
            @Payload byte[] payload                    // Extract the protobuf payload
    ) {
        logger.info("Received command message with type: {} and payload of size: {}", messageType, payload.length);

        // Route message based on type
        switch (messageType) {
            case "ProcessNewShipment":
                handleProcessNewShipment(payload);
                break;


            default:
                logger.warn("Unhandled message type: {}", messageType);
        }
    }



    /**
     * Handles the 'ProcessNewShipment' command.
     * Prevents starting a new process if an active instance already exists.
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
        }
    }
}