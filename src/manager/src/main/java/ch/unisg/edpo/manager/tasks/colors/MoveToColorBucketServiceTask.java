package ch.unisg.edpo.manager.tasks.colors;

import ch.unisg.edpo.proto.commands.machines.v1.SortBlock;
import ch.unisg.edpo.proto.models.v1.BlockColor;
import ch.unisg.edpo.manager.producer.CommandProducer;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MoveToColorBucketServiceTask implements JavaDelegate {

    private final CommandProducer commandProducer;

    public MoveToColorBucketServiceTask(CommandProducer commandProducer) {
        this.commandProducer = commandProducer;
    }
    @Override
    public void execute(DelegateExecution execution) {
        // Log the class name
        log.info("Executing task in class: {}", this.getClass().getSimpleName());

        // Retrieve process variables if needed
        String currentBlockColorString = (String) execution.getVariable("currentBlockColor");
        log.info("Retrieved Current Block Colour: {}", currentBlockColorString);


        // Convert the string to BlockColor
        BlockColor currentBlockColor = BlockColor.valueOf(currentBlockColorString);


        // Build the Protobuf command
        SortBlock command = SortBlock.newBuilder()
                .setColor(currentBlockColor)
                .build();

        // Add a delay of 2 seconds
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread was interrupted", e);
        }

        // Send the command using the CommandProducer
        commandProducer.sendCommand(command, "SortBlock");


        // Add your business logic here
        log.info("Task execution completed.");
    }
}