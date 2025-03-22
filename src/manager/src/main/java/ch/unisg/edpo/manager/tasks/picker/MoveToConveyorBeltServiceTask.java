package ch.unisg.edpo.manager.tasks.picker;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MoveToConveyorBeltServiceTask implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        // Log task execution
        log.info("Executing task in class: {}", this.getClass().getSimpleName());

        // Example: Retrieve process variable
        //String pickerId = (String) execution.getVariable("pickerId");
        //log.info("Picker ID: {}", pickerId);

        // Example logic: Move item to the conveyor belt
        //log.info("Picker {} is moving the item to the conveyor belt.", pickerId);

        // Set a custom process variable to indicate task completion
        //execution.setVariable("moveToConveyorStatus", "completed");

        // Final log message
        log.info("Task execution for MoveToConveyorBeltServiceTask completed.");
    }
}