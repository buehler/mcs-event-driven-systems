package ch.unisg.edpo.manager.tasks.initialize;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InitializeProcessDelegate implements JavaDelegate {

    // Timeout durations
    @Value("${robot.timeout.duration}")
    private String defaultTimeout;

    @Value("${robot.timeout.duration2}")
    private String defaultTimeoutTwo;

    // Auto retry settings
    @Value("${robot.auto.retry.count}")
    private Integer maxRetry;

    // Retry for specific processes
    @Value("${robot.auto.retry.grid}")
    private Boolean autoRetryGrid;

    @Value("${robot.auto.retry.conveyor}")
    private Boolean autoRetryConveyor;

    @Value("${robot.auto.retry.nfc}")
    private Boolean autoRetryNfc;

    @Value("${robot.auto.retry.color}")
    private Boolean autoRetryColor;

    @Value("${robot.auto.retry.sorting}")
    private Boolean autoRetrySorting;

    @Override
    public void execute(DelegateExecution execution) {
        // Set general process variables
        execution.setVariable("timeoutDuration", defaultTimeout);
        execution.setVariable("timeoutDurationTwo", defaultTimeoutTwo);
        execution.setVariable("maxRetry", maxRetry);

        // Set specific retry process variables
        execution.setVariable("autoRetryGrid", autoRetryGrid);
        execution.setVariable("autoRetryConveyor", autoRetryConveyor);
        execution.setVariable("autoRetryNfc", autoRetryNfc);
        execution.setVariable("autoRetryColor", autoRetryColor);
        execution.setVariable("autoRetrySorting", autoRetrySorting);

        // Log the initialization for debugging purposes
        log.info("Initializing process with timeout value: {}", defaultTimeout);
        log.info("Initializing process with timeoutTwo value: {}", defaultTimeoutTwo);
        log.info("Initializing process with max retry count: {}", maxRetry);
        log.info("Initializing process with autoRetryGrid: {}", autoRetryGrid);
        log.info("Initializing process with autoRetryConveyor: {}", autoRetryConveyor);
        log.info("Initializing process with autoRetryNfc: {}", autoRetryNfc);
        log.info("Initializing process with autoRetryColor: {}", autoRetryColor);
        log.info("Initializing process with autoRetrySorting: {}", autoRetrySorting);
    }
}