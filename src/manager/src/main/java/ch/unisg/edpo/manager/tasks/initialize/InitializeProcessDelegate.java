package ch.unisg.edpo.manager.tasks.initialize;

import ch.unisg.edpo.manager.testing.TestingStatusService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InitializeProcessDelegate implements JavaDelegate {

    private final TestingStatusService testingStatusService;

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

    public InitializeProcessDelegate(TestingStatusService testingStatusService) {
        this.testingStatusService = testingStatusService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        String timeoutToUse = defaultTimeout;
        String timeoutTwoToUse = defaultTimeoutTwo;

        // Check if testing mode is active and use the test timeouts if they are set
        if (testingStatusService.isTestingRunning()) {
            if (testingStatusService.getDefaultTimeout() != null) {
                // Get the timeout and convert to String - it comes in as Duration from TestingStatusService
                timeoutToUse = testingStatusService.getDefaultTimeout().toString();
                log.info("Using test timeout: {}", timeoutToUse);
            }

            if (testingStatusService.getDefaultTimeoutTwo() != null) {
                // Get the second timeout and convert to String
                timeoutTwoToUse = testingStatusService.getDefaultTimeoutTwo().toString();
                log.info("Using test timeout two: {}", timeoutTwoToUse);
            }
        }

        // Set general process variables
        execution.setVariable("timeoutDuration", timeoutToUse);
        execution.setVariable("timeoutDurationTwo", timeoutTwoToUse);
        execution.setVariable("maxRetry", maxRetry);

        // Set specific retry process variables
        execution.setVariable("autoRetryGrid", autoRetryGrid);
        execution.setVariable("autoRetryConveyor", autoRetryConveyor);
        execution.setVariable("autoRetryNfc", autoRetryNfc);
        execution.setVariable("autoRetryColor", autoRetryColor);
        execution.setVariable("autoRetrySorting", autoRetrySorting);

        // Override the auto-retry setting based on failingPoint if testing and it's not "none"
        if (testingStatusService.isTestingRunning()) {
            String failingPoint = testingStatusService.getFailingPoint();
            if (failingPoint != null && !failingPoint.equals("none")) {
                log.info("Testing: Setting failing point: {}", failingPoint);

                // Set the specific auto-retry setting to true
                execution.setVariable(failingPoint, true);
                log.info("Testing: Enabled auto-retry for {}", failingPoint);
            }
        }

        // Log the initialization for debugging purposes
        log.info("Initializing process with timeout value: {}", timeoutToUse);
        log.info("Initializing process with timeoutTwo value: {}", timeoutTwoToUse);
        log.info("Initializing process with max retry count: {}", maxRetry);
        log.info("Initializing process with autoRetryGrid: {}", autoRetryGrid);
        log.info("Initializing process with autoRetryConveyor: {}", autoRetryConveyor);
        log.info("Initializing process with autoRetryNfc: {}", autoRetryNfc);
        log.info("Initializing process with autoRetryColor: {}", autoRetryColor);
        log.info("Initializing process with autoRetrySorting: {}", autoRetrySorting);
    }
}