package ch.unisg.edpo.manager.tasks.process;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class PreProcessDataTask implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        log.info("Starting PreProcessDataTask to determine the current block...");

        // Initialize 'retryCount' to 0
        execution.setVariable("retryCount", 0);
        log.info("Initialized 'retryCount' variable to 0.");

        // Retrieve 'blockPositions' from process variables
        List<String> blockPositions = (List<String>) execution.getVariable("blockPositions");

        if (blockPositions == null || blockPositions.isEmpty()) {
            log.warn("'blockPositions' is null or empty. No blocks to process.");
            return;
        }

        // Get the first block (position) from the list
        String currentBlock = blockPositions.get(0); // First element

        // Save current block as a new process variable
        execution.setVariable("currentBlock", currentBlock);

        // Remove the current block from 'blockPositions'
        blockPositions.remove(0); // Remove the first element

        // Update 'blockPositions' in process variables
        execution.setVariable("blockPositions", blockPositions);

        // Log the updated variables for debugging
        log.info("Set 'currentBlock' variable: {}", currentBlock);
        log.info("Updated 'blockPositions' after removing '{}': {}", currentBlock, blockPositions);
    }
}