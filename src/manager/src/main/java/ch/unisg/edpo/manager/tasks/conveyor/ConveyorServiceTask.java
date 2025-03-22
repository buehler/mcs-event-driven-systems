package ch.unisg.edpo.manager.tasks.conveyor;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConveyorServiceTask implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        // Log the class name and add business logic
        log.info("Executing task in class: {}", this.getClass().getSimpleName());

        // Example: Using a process variable
        //String conveyorSpeed = (String) execution.getVariable("conveyorSpeed");
        //log.info("Conveyor speed retrieved from process variable: {}", conveyorSpeed);

        // Add or update a process variable
        //execution.setVariable("conveyorStatus", "active");

        // Business logic placeholder
        log.info("Conveyor has been activated.");

        // Indicate that the execution of the ConveyorServiceTask is completed
        log.info("Task execution for ConveyorServiceTask completed.");
    }
}