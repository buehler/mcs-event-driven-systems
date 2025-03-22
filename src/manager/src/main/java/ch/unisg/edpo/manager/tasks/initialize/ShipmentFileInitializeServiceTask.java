package ch.unisg.edpo.manager.tasks.initialize;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ShipmentFileInitializeServiceTask implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        // Fetch the 'blocks' variable from process scope (stored as JSON)
        Object blocks = execution.getProcessEngineServices()
                .getRuntimeService()
                .getVariable(execution.getProcessInstanceId(), "blocks");

        if (blocks == null) {
            log.warn("'blocks' variable is null; cannot proceed.");
            return;
        }

        // Attempt to parse the JSON variable using Spin
        SpinJsonNode blocksJson;
        if (blocks instanceof SpinJsonNode) {
            blocksJson = (SpinJsonNode) blocks;
            log.info("Successfully retrieved 'blocks' as SpinJsonNode: {}", blocksJson);
        } else if (blocks instanceof String) {
            blocksJson = Spin.JSON(blocks);
            log.info("Successfully parsed 'blocks' from String into SpinJsonNode: {}", blocksJson);
        } else {
            log.warn("'blocks' variable is of an unexpected type: {}", blocks.getClass());
            return;
        }

        // Deserialize 'blocks' JSON array into a Java List of Booleans
        List<Boolean> blocksList = blocksJson.mapTo(List.class);

        // Ensure the list has 9 elements (as expected)
        if (blocksList.size() != 9) {
            log.error("Unexpected size for 'blocks' list; expected 9 elements, found {}", blocksList.size());
            return;
        }

        // Array of positions corresponding to block indices
        String[] positions = {
                "top_left", "top_middle", "top_right",
                "middle_left", "middle_middle", "middle_right",
                "bottom_left", "bottom_middle", "bottom_right"
        };

        // Create a List to store only block positions with true values
        List<String> blockPositions = new ArrayList<>();

        // Iterate through the blocks and add positions of 'true' blocks to the list
        for (int i = 0; i < blocksList.size(); i++) {
            if (blocksList.get(i)) { // Only include positions with `true` status
                blockPositions.add(positions[i]);
            }
        }

        // Calculate the number of remaining blocks
        long blocksRemaining = blockPositions.size(); // Only count the true positions

        // Log the newly created variables for debugging
        log.info("Generated 'blockPositions': {}", blockPositions);
        log.info("Calculated 'blocksRemaining': {}", blocksRemaining);

        // Store the new variables in the process instance scope
        execution.setVariable("blockPositions", blockPositions); // Store as a list of positions
        execution.setVariable("blocksRemaining", blocksRemaining);
    }
}