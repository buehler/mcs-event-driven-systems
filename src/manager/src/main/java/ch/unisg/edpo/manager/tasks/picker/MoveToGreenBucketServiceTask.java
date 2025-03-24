package ch.unisg.edpo.manager.tasks.picker;

import ch.unisg.edpo.proto.commands.machines.v1.SortBlock;
import ch.unisg.edpo.manager.producer.CommandProducer;
import ch.unisg.edpo.proto.models.v1.BlockColor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MoveToGreenBucketServiceTask implements JavaDelegate {

    private final CommandProducer commandProducer;

    public MoveToGreenBucketServiceTask(CommandProducer commandProducer) {this.commandProducer = commandProducer;}

    @Override
    public void execute(DelegateExecution execution) {
        // Log that the task execution has started
        log.info("Executing task in class: {}", this.getClass().getSimpleName());


        // Retrieve process variables if needed
        String currentBlock = (String) execution.getVariable("currentBlock");
        log.info("Block to move from NFC to Green Bucket: {}", currentBlock);

        BlockColor currentBlockColor = BlockColor.forNumber(BlockColor.BLOCK_COLOR_GREEN_VALUE);

        // Build the Protobuf command
        SortBlock command = SortBlock.newBuilder()
                .setColor(currentBlockColor)
                .build();

        // Send the command using the CommandProducer
        commandProducer.sendCommand(command, "SortBlock");



        // Example: Retrieve process variable
        //String pickerId = (String) execution.getVariable("pickerId");
        //log.info("Picker ID: {}", pickerId);

        // Simulated business logic for "Move to Green Bucket"
        //log.info("Picker {} is moving the item to the green bucket.", pickerId);

        // Update the process variable to indicate the task completion
        //execution.setVariable("moveToGreenBucketStatus", "completed");

        // Log task execution completion
        log.info("Task execution for MoveToGreenBucketServiceTask completed.");
    }
}