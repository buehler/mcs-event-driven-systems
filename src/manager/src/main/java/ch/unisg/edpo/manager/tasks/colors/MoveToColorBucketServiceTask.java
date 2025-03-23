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
        String currentBlock = (String) execution.getVariable("currentBlock");
        log.info("Block to move from NFC to Green Bucket: {}", currentBlock);

        BlockColor currentBlockColor = BlockColor.forNumber(BlockColor.BLOCK_COLOR_GREEN_VALUE);

        // Build the Protobuf command
        SortBlock command = SortBlock.newBuilder()
                .setColor(currentBlockColor)
                .build();

        // Send the command using the CommandProducer
        commandProducer.sendCommand(command, "SortBlock");



        // Add your business logic here
        log.info("Task execution completed.");
    }
}