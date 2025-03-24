package ch.unisg.edpo.manager.tasks.errorHandling;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ErrorManagerServiceTask implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        log.info("Executing ErrorManagerServiceTask...");


        // here we could throw an error event


        // Example: Retrieve process variable
        //String pickerId = (String) execution.getVariable("pickerId");
        //log.info("Picker ID: {}", pickerId);

        // Simulated business logic for "Move to Green Bucket"
        //log.info("Picker {} is moving the item to the green bucket.", pickerId);

        // Update the process variable to indicate the task completion
        //execution.setVariable("moveToGreenBucketStatus", "completed");

        // Log task execution completion
        log.info("Task execution for ErrorManagerServiceTask completed.");

    }
}

