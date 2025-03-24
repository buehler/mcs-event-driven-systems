package ch.unisg.edpo.manager.tasks.initialize;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InitializeProcessDelegate implements JavaDelegate {

    @Value("${robot.timeout.duration}")
    private String defaultTimeout;

    @Value("${robot.timeout.duration2}")
    private String defaultTimeoutTwo;

    @Value("${robot.auto.retry}")
    private Boolean autoRetry;

    @Value("${robot.auto.retry.count}")
    private Integer maxRetry;

    @Override
    public void execute(DelegateExecution execution) {
        execution.setVariable("timeoutDuration", defaultTimeout);
        execution.setVariable("timeoutDurationTwo", defaultTimeoutTwo);
        execution.setVariable("autoRetry", autoRetry);
        execution.setVariable("maxRetry", maxRetry);

        log.info("Initializing process with timeout value: {}", defaultTimeout);
        log.info("Initializing process with timeout two value: {}", defaultTimeoutTwo);
        log.info("Initializing process with auto-retry: {}", autoRetry);
        log.info("Initializing process with max retry: {}", maxRetry);
    }
}