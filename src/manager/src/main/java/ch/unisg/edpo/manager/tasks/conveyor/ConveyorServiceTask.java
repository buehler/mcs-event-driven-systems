package ch.unisg.edpo.manager.tasks.conveyor;

import ch.unisg.edpo.proto.commands.machines.v1.ConveyorMoveBlock;
import ch.unisg.edpo.manager.producer.CommandProducer;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConveyorServiceTask implements JavaDelegate {

    private final CommandProducer commandProducer;

    // Constructor injection for the CommandProducer
    public ConveyorServiceTask(CommandProducer commandProducer) {
        this.commandProducer = commandProducer;
    }

    @Override
    public void execute(DelegateExecution execution) {
        log.info("Executing task in class: {}", this.getClass().getSimpleName());

        // Retrieve process variables if required
        String currentBlock = (String) execution.getVariable("currentBlock");
        log.info("Block to move on conveyor: {}", currentBlock);

        // Build the Protobuf command
        ConveyorMoveBlock command = ConveyorMoveBlock.newBuilder()
                .build();

        // Send the command using the CommandProducer
        commandProducer.sendCommand(command, "ConveyorMoveBlock");

        // Indicate that the execution of the ConveyorServiceTask is completed
        log.info("Task execution for ConveyorServiceTask completed.");
    }
}