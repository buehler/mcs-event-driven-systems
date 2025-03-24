package ch.unisg.edpo.manager.tasks.inventory;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UpdateInventoryServiceTask implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        // Log the class name when the task starts
        log.info("Executing task in class: {}", this.getClass().getSimpleName());

        // Example: Retrieve and process a process variable
        //String productId = (String) execution.getVariable("productId");
        //Integer quantity = (Integer) execution.getVariable("quantity");

        // Simulate inventory update logic (replace this with your actual business logic)
        //log.info("Updating inventory for productId: {}, quantity: {}", productId, quantity);

        // Set a process variable indicating a successful update (or other task outputs)
        //execution.setVariable("inventoryUpdateStatus", "success");

        // Log completion
        log.info("Task execution for UpdateInventoryServiceTask completed successfully.");
    }
}