package ch.unisg.edpo.manager.tasks.picker;

import ch.unisg.edpo.manager.producer.CommandProducer;
import ch.unisg.edpo.proto.commands.machines.v1.MoveBlockFromShipmentToNfc;
import ch.unisg.edpo.proto.models.v1.PickupPosition;
import ch.unisg.edpo.manager.producer.EventProducer;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MoveToNFCServiceTask implements JavaDelegate {

    private final CommandProducer commandProducer;

    public MoveToNFCServiceTask(CommandProducer commandProducer) {
        this.commandProducer = commandProducer;
    }

    @Override
    public void execute(DelegateExecution execution) {
        log.info("Executing MoveToNFCServiceTask...");

        // Retrieve the 'currentBlock' variable
        String currentBlock = (String) execution.getVariable("currentBlock");
        log.info("Retrieved 'currentBlock': {}", currentBlock);

        // Map 'currentBlock' directly using Lookup functions
        PickupPosition pickupPosition = resolvePickupPosition(currentBlock);

        // If the position is invalid, throw error
        if (pickupPosition == PickupPosition.PICKUP_POSITION_UNSPECIFIED) {
            log.error("Invalid 'currentBlock' value: {}. Unable to process task.", currentBlock);
            throw new IllegalArgumentException("Invalid 'currentBlock' value: " + currentBlock);
        }

        // Build the Protobuf command
        MoveBlockFromShipmentToNfc command = MoveBlockFromShipmentToNfc.newBuilder()
                .setPosition(pickupPosition)
                .build();

        // Send the command using the EventProducer
        commandProducer.sendCommand(command, "MoveBlockFromShipmentToNfc");
    }

    /**
     * Resolves 'currentBlock' string to the Protobuf PickupPosition enum.
     *
     * @param currentBlock The string representation of the block position
     * @return The corresponding PickupPosition enum, or PICKUP_POSITION_UNSPECIFIED if invalid
     */
    private PickupPosition resolvePickupPosition(String currentBlock) {
        try {
            return PickupPosition.valueOf("PICKUP_POSITION_" + currentBlock.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("'currentBlock' value '{}' is invalid, returning PICKUP_POSITION_UNSPECIFIED.", currentBlock);
            return PickupPosition.PICKUP_POSITION_UNSPECIFIED;
        }
    }
}