package ch.unisg.edpo.manager.tasks.process;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PostProcessDataTask implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        log.info("Starting PostProcessDataTask to decrement blocksRemaining...");

        // Retrieve 'blocksRemaining' variable from process context
        Object blocksRemainingObj = execution.getVariable("blocksRemaining");

        if (blocksRemainingObj == null) {
            log.warn("'blocksRemaining' variable is null. Cannot decrement.");
            return;
        }

        // Parse the 'blocksRemaining' variable as a long
        long blocksRemaining;
        try {
            blocksRemaining = Long.parseLong(blocksRemainingObj.toString());
        } catch (NumberFormatException e) {
            log.error("Failed to parse 'blocksRemaining' as a number: {}", blocksRemainingObj, e);
            return;
        }

        // Decrement blocksRemaining by 1
        blocksRemaining -= 1;

        // Update the process variable
        execution.setVariable("blocksRemaining", blocksRemaining);

        // Log the updated value
        log.info("Updated 'blocksRemaining' after decrement: {}", blocksRemaining);
    }
}