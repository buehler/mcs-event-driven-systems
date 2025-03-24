package ch.unisg.edpo.manager.tasks.errorHandling;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AutoRetryServiceTask implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        log.info("Executing AutoRetryServiceTask...");

        // Retrieve 'maxRetry' and 'retryCount' variables
        Integer maxRetry = (Integer) execution.getVariable("maxRetry");
        Long retryCount = (Long) execution.getVariable("retryCount");

        if (maxRetry == null || retryCount == null) {
            log.error("'maxRetry' or 'retryCount' is not set. Ensure both variables are defined in the process context.");
            throw new IllegalArgumentException("'maxRetry' and 'retryCount' must be set.");
        }

        log.info("Retrieved process variables: maxRetry = {}, retryCount = {}", maxRetry, retryCount);

        // Determine if an auto retry can still be performed
        boolean canAutoRetry = retryCount < maxRetry; // Java auto-unboxes and converts Integer to Long for comparison here

        if (canAutoRetry) {
            log.info("Auto retry is allowed. Incrementing 'retryCount' and setting 'autoRetry' to true.");
            retryCount++; // Increment the retry count
            execution.setVariable("retryCount", retryCount); // Update the variable in the execution context
        } else {
            log.info("Retry limit reached. Setting 'autoRetry' to false.");
        }

        // Set the 'canAutoRetry' variable accordingly
        execution.setVariable("canAutoRetry", canAutoRetry);
    }
}