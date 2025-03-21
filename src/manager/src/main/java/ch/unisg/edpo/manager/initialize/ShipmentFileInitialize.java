package ch.unisg.edpo.manager.initialize;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class ShipmentFileInitialize implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        try {
            // Log all variables for debugging
            log.info("All variables in current execution scope: {}", execution.getVariables());

            // Fetch the 'blocks' variable (JSON format)
            Object rawBlocks = execution.getVariable("blocks");
            log.info("Raw blocks value: {}", rawBlocks);

            // Deserialize if it is JSON
            if (rawBlocks instanceof SpinJsonNode) {
                SpinJsonNode blocksJson = (SpinJsonNode) rawBlocks;
                log.info("Blocks JSON value: {}", blocksJson.toString());

                // Optionally, access it as a Java List
                List<Boolean> blocks = blocksJson.mapTo(List.class);
                log.info("Blocks List: {}", blocks);
            } else if (rawBlocks == null) {
                log.warn("Variable 'blocks' is null.");
            } else {
                log.warn("Variable 'blocks' exists but is not JSON: {}", rawBlocks.getClass());
            }

        } catch (Exception e) {
            log.error("Error while handling 'blocks' variable in ShipmentFileInitialize", e);
        }
    }
}

/**
Zwei Prozessvariablen sollen erstellt werden.

blockPositions = {
        "top-left": false,
        "top-middle": true,
        "top-right": false,
        "middle-left": false,
        "middle-middle": true,
        "middle-right": false,
        "bottom-left": false,
        "bottom-middle": true,
        "bottom-right": false
        }
blocksRemaining = 3


 */