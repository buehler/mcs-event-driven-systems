package ch.unisg.edpo.manager.testing;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class TestingStatusService {

    private final Logger logger = LoggerFactory.getLogger(TestingStatusService.class);
    private final AtomicBoolean isTestingRunning = new AtomicBoolean(false);
    /**
     * -- GETTER --
     *  Get the default timeout
     */
    @Getter
    private Duration defaultTimeout;
    /**
     * -- GETTER --
     *  Get the secondary default timeout
     */
    @Getter
    private Duration defaultTimeoutTwo;

    /**
     * -- GETTER/SETTER --
     * Simulated time for robot operations in seconds
     */
    @Getter
    private int simulatedRobotTime;

    @Getter
    private String failingPoint = "none";



    /**
     * Check if testing mode is currently running
     * @return true if testing mode is active, false otherwise
     */
    public boolean isTestingRunning() {
        return isTestingRunning.get();
    }

    /**
     * Start testing mode
     * @return true if successfully started, false if already running
     */
    public boolean startTesting() {
        boolean result = isTestingRunning.compareAndSet(false, true);
        if (result) {
            logger.info("Testing mode started");
        } else {
            logger.info("Testing mode already active");
        }
        return result;
    }

    /**
     * Stop testing mode
     */
    public void stopTesting() {
        isTestingRunning.set(false);
        logger.info("Testing mode stopped");
    }


    /**
     * Set timeouts for the test run
     */
    public void setTimeouts(Duration defaultTimeout, Duration defaultTimeoutTwo, int simulatedRobotTime) {
        this.defaultTimeout = defaultTimeout;
        this.defaultTimeoutTwo = defaultTimeoutTwo;
        this.simulatedRobotTime = simulatedRobotTime;
        logger.info("Testing: Test timeouts set: defaultTimeout={}, defaultTimeoutTwo={}",
                defaultTimeout, defaultTimeoutTwo);
        logger.info("Testing: Simulated robot time set to {}", simulatedRobotTime);
    }

    /**
     * Sets the failing point for the test
     * @param failingPoint The point at which the test should simulate a failure
     */
    public void setFailingPoint(String failingPoint) {
        this.failingPoint = failingPoint != null ? failingPoint : "none";
        logger.info("Testing: Failing point set to: {}", this.failingPoint);
    }


    // Add to TestingStatusService.java
    public static class BlockStatus {
        private final int blockNumber;
        private final String color;
        private int checkedForNFC;
        private int colorChecked;

        public BlockStatus(int blockNumber, String color) {
            this.blockNumber = blockNumber;
            this.color = color;
            this.checkedForNFC = 0;
            this.colorChecked = 0;
        }

        // Getters and setters
        public int getBlockNumber() { return blockNumber; }
        public String getColor() { return color; }
        public int getCheckedForNFC() { return checkedForNFC; }
        public void setCheckedForNFC(int checkedForNFC) { this.checkedForNFC = checkedForNFC; }
        public int getColorChecked() { return colorChecked; }
        public void setColorChecked(int colorChecked) { this.colorChecked = colorChecked; }
    }

    private List<BlockStatus> blocks = new ArrayList<>();

    public void setBlocks(List<BlockStatus> blocks) {
        this.blocks = blocks;
        logger.info("Set {} blocks for testing", blocks.size());
    }

    public List<BlockStatus> getBlocks() {
        return blocks;
    }

    public BlockStatus getNextBlockForNFC() {
        return blocks.stream()
                .filter(b -> b.getCheckedForNFC() == 0)
                .findFirst()
                .orElse(null);
    }

    public BlockStatus getNextBlockForColorCheck() {
        return blocks.stream()
                .filter(b -> b.getColorChecked() == 0)
                .findFirst()
                .orElse(null);
    }

}