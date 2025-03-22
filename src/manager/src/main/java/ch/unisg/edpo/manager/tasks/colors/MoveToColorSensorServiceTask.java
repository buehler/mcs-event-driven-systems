package ch.unisg.edpo.manager.tasks.colors;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MoveToColorSensorServiceTask implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        // Log the class name
        log.info("Executing task in class: {}", this.getClass().getSimpleName());

        // Example of using a process variable
        // String sensorType = (String) execution.getVariable("sensorType");
        // log.info("Sensor Type: {}", sensorType);

        // Add custom logic
        // execution.setVariable("sensorStatus", "moved");

        // Indicate completion
        log.info("Task execution for MoveToColorSensorServiceTask completed.");
    }
}