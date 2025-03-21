package ch.unisg.edpo.manager.initialize;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class ShipmentFileInitialize implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        // Fetch 'blocks' variable from process scope (stored as JSON)
        Object blocks = execution.getProcessEngineServices()
                .getRuntimeService()
                .getVariable(execution.getProcessInstanceId(), "blocks");

        if (blocks == null) {
            log.warn("'blocks' variable is null, cannot proceed.");
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
            log.error("Unexpected size for 'blocks' list. Expected 9 elements, found {}", blocksList.size());
            return;
        }

        // Map positions to their corresponding block values
        Map<String, Boolean> blockPositions = new HashMap<>();
        blockPositions.put("top-left", blocksList.get(0));
        blockPositions.put("top-middle", blocksList.get(1));
        blockPositions.put("top-right", blocksList.get(2));
        blockPositions.put("middle-left", blocksList.get(3));
        blockPositions.put("middle-middle", blocksList.get(4));
        blockPositions.put("middle-right", blocksList.get(5));
        blockPositions.put("bottom-left", blocksList.get(6));
        blockPositions.put("bottom-middle", blocksList.get(7));
        blockPositions.put("bottom-right", blocksList.get(8));

        // Calculate the remaining blocks by counting 'true' values in the list
        long blocksRemaining = blocksList.stream()
                .filter(Boolean::booleanValue)
                .count();

        // Log the newly created variables for debugging
        log.info("Generated 'blockPositions': {}", blockPositions);
        log.info("Calculated 'blocksRemaining': {}", blocksRemaining);

        // Store the new variables in the process instance scope
        execution.setVariable("blockPositions", blockPositions);
        execution.setVariable("blocksRemaining", blocksRemaining);
    }
}