package ch.unisg.edpo.manager.tasks.colors;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MoveToColorBucketServiceTask implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        // Log the class name
        log.info("Executing task in class: {}", this.getClass().getSimpleName());

        // Example of adding a variable to the execution context
        //execution.setVariable("taskStatus", "completed");

        // Add your business logic here
        log.info("Task execution completed.");
    }
}