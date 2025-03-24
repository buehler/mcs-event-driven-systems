package ch.unisg.edpo.manager.tasks.colors;

import ch.unisg.edpo.proto.commands.machines.v1.MoveBlockFromConveyorToColorDetector;
import ch.unisg.edpo.manager.producer.CommandProducer;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MoveToColorSensorServiceTask implements JavaDelegate {

    private final CommandProducer commandProducer;

    // Constructor injection for the CommandProducer
    public MoveToColorSensorServiceTask(CommandProducer commandProducer) {
        this.commandProducer = commandProducer;
    }

    @Override
    public void execute(DelegateExecution execution) {
        // Log the class name
        log.info("Executing task in class: {}", this.getClass().getSimpleName());

        // Retrieve process variables if required
        String currentBlock = (String) execution.getVariable("currentBlock");
        log.info("Block to move from Conveyor to Color Detector: {}", currentBlock);

        try {
            // Build the Protobuf command
            MoveBlockFromConveyorToColorDetector command = MoveBlockFromConveyorToColorDetector.newBuilder()
                    .build();

            // Send the command using the CommandProducer
            commandProducer.sendCommand(command, "MoveBlockFromConveyorToColorDetector");

        } catch (Exception e) {
            log.error("Error while executing MoveToColorSensorServiceTask for block: {}", currentBlock, e);
        }

        // Indicate that the execution of the task is completed
        log.info("Task execution for MoveToColorSensorServiceTask completed.");
    }
}