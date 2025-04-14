package ch.unisg.edpo.manager.tasks.conveyor;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConveyorEventServiceTask implements JavaDelegate {
    private final RuntimeService runtimeService; // Camunda RuntimeService for correlating messages

    // Constructor-based dependency injection
    public ConveyorEventServiceTask(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        log.info("Executing task in class: {}", this.getClass().getSimpleName());

        try {
            log.info("Handling 'conveyorBeltReady' event...");
            runtimeService.createMessageCorrelation("conveyorBeltReady").correlateAll();
            log.info("Message `conveyorBeltReady` successfully correlated in Camunda.");
        } catch (Exception e) {
            log.error("Error while correlating 'conveyorBeltReady' event in Camunda:", e);
        }
    }
}