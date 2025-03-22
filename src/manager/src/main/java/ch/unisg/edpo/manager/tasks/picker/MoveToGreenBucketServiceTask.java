package ch.unisg.edpo.manager.tasks.picker;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MoveToGreenBucketServiceTask implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        // Log that the task execution has started
        log.info("Executing task in class: {}", this.getClass().getSimpleName());

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