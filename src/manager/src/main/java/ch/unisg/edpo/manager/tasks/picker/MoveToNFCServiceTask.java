package ch.unisg.edpo.manager.tasks.picker;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MoveToNFCServiceTask implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        // Log the class name for tracing
        log.info("Executing task in class: {}", this.getClass().getSimpleName());

        // Retrieve process variables (if required)
        //String pickerId = (String) execution.getVariable("pickerId");
        //log.info("Picker ID: {}", pickerId);

        // Simulated business logic: Move to NFC
        //log.info("Picker {} is interacting with NFC station.", pickerId);

        // Set a process variable to indicate task status
        //execution.setVariable("moveToNFCStatus", "completed");
        //log.info("'moveToNFCStatus' variable set to 'completed'.");

        // Log conclusion of task execution
        log.info("Task execution for MoveToNFCServiceTask completed successfully.");
    }
}