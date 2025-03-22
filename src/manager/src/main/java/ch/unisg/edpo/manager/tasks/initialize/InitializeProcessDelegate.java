package ch.unisg.edpo.manager.tasks.initialize;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InitializeProcessDelegate implements JavaDelegate {

    @Value("${robot.timeout.duration:10000}") // Default to 10000 ms if the property is missing
    private String defaultTimeout;

    @Override
    public void execute(DelegateExecution execution) {
        execution.setVariable("timeoutDuration", defaultTimeout);

        log.info("Initializing process with timeout value: {}", defaultTimeout);
    }
}