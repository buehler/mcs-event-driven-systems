package ch.unisg.edpo.manager.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import ch.unisg.edpo.proto.commands.inventory.v1.ProcessNewShipment; // Import the generated protobuf class

import java.util.List;
import java.util.Map;

@Service
public class CommandListener {
    private final Logger logger = LoggerFactory.getLogger(CommandListener.class);

    private final RuntimeService runtimeService; // Inject RuntimeService for process handling

    // Constructor injection for `RuntimeService`
    public CommandListener(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @KafkaListener(
            topics = "${topics.commands}",       // Use the topic from application properties
            containerFactory = "kafkaListenerFactory" // Kafka container factory defined in your config
    )
    public void listen(
            @Header("messageType") String messageType, // Extract Kafka header
            @Payload byte[] payload                    // Extract message payload in protobuf format
    ) {
        logger.info("Received command message with type: {} and payload of size: {}", messageType, payload.length);

        // Handle specific message types using a switch-case construct
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
     * Delegates the information to the Camunda BPM runtime to start the process.
     */
    private void handleProcessNewShipment(byte[] payload) {
        try {
            // Parse the protobuf payload using generated classes
            ProcessNewShipment shipmentCommand = ProcessNewShipment.parseFrom(payload);

            // Extract information from the protobuf object
            String shipmentId = shipmentCommand.getShipmentId();           // Get the shipment ID
            List<Boolean> blocks = shipmentCommand.getBlocksList();       // Get the Boolean blocks list

            // Use Spin to serialize the blocks list into a JSON format
            SpinJsonNode blocksJson = Spin.JSON(blocks);

            // Start a Camunda process instance using the extracted data
            runtimeService.startProcessInstanceByMessage(
                    "ProcessNewShipment",             // Message name, referenced in BPMN file
                    shipmentId,                       // Business key for the process
                    Map.of("blocks", blocksJson)      // Variables passed into the process
            );

            logger.info("Started process instance for shipmentId: '{}' with blocks: {}", shipmentId, blocks);

        } catch (Exception e) {
            logger.error("Error while processing 'ProcessNewShipment' command:", e);
        }
    }
}