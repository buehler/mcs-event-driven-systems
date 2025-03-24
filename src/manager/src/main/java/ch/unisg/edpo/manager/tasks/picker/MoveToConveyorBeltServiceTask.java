package ch.unisg.edpo.manager.tasks.picker;

import ch.unisg.edpo.proto.commands.machines.v1.MoveBlockFromNfcToConveyor;
import ch.unisg.edpo.manager.producer.CommandProducer;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MoveToConveyorBeltServiceTask implements JavaDelegate {

    private final CommandProducer commandProducer;

    // Constructor injection for the CommandProducer
    public MoveToConveyorBeltServiceTask(CommandProducer commandProducer) {
        this.commandProducer = commandProducer;
    }

    @Override
    public void execute(DelegateExecution execution) {
        log.info("Executing task in class: {}", this.getClass().getSimpleName());

        // Retrieve process variables if needed
        String currentBlock = (String) execution.getVariable("currentBlock");
        log.info("Block to move from NFC to conveyor: {}", currentBlock);

        // Build the Protobuf command
        MoveBlockFromNfcToConveyor command = MoveBlockFromNfcToConveyor.newBuilder()
                .build();

        // Send the command using the CommandProducer
        commandProducer.sendCommand(command, "MoveBlockFromNfcToConveyor");

        log.info("Task execution for MoveToConveyorBeltServiceTask completed.");
    }
}