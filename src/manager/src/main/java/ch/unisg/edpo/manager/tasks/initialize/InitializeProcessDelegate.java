package ch.unisg.edpo.manager.tasks.initialize;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InitializeProcessDelegate implements JavaDelegate {

    @Value("${robot.timeout.duration}") // Default to 10000 ms if the property is missing
    private String defaultTimeout;

    @Value("${robot.timeout.duration2}") // Default to 10000 ms if the property is missing
    private String defaultTimeoutTwo;

    @Override
    public void execute(DelegateExecution execution) {
        execution.setVariable("timeoutDuration", defaultTimeout);
        execution.setVariable("timeoutDurationTwo", defaultTimeoutTwo);

        log.info("Initializing process with timeout value: {}", defaultTimeout);
        log.info("Initializing process with timeout two value: {}", defaultTimeoutTwo);
    }
}