package com.experiments;

import com.google.common.io.Resources;
import com.producer.ClicksProducerConsumerExperimentLagDataLoss;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.io.InputStream;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class ConsumerExperimentLagDataLoss {
    private static final int POLL_DURATION_MS = 100; // Polling interval for Kafka consumer
    private static final int TEST_DURATION_MS = 5000; // Each test runs for 5 seconds

    public static void main(String[] args) throws Exception {
        // Define stepwise processing delays (in ms) and messages per second (rates)
        int[] processingDelays = {0, 200, 400, 600};
        int[] messagesPerSecondRates = {500, 1000, 1500};

        String topic = "click-events"; // Target Kafka topic

        // Load Kafka consumer properties
        Properties consumerProperties = new Properties();
        try (InputStream props = Resources.getResource("consumer.properties").openStream()) {
            consumerProperties.load(props);
        }

        // Load Kafka producer properties
        Properties producerProperties = new Properties();
        try (InputStream props = Resources.getResource("producer.properties").openStream()) {
            producerProperties.load(props);
        }

        // Initialize producer controller
        ClicksProducerConsumerExperimentLagDataLoss producerController = new ClicksProducerConsumerExperimentLagDataLoss();

        // Print setup to indicate test start
        System.out.println("Starting Kafka Consumer-Producer Lag Data Loss Experiment...");

        // Step 1: Delete the topic if it exists
        deleteAllTopics(consumerProperties);

        // Step 2: Create a new topic
        createTopic(topic, 1, consumerProperties);

        // Step 3: Wait for the topic to be ready
   //     waitForTopicToBeReady(topic, consumerProperties);

        // Step 4: Aggregate report for all test runs
        List<TestResult> combinedReport = new ArrayList<>();

        // Step 5: Run the tests
        for (int messagesPerSecond : messagesPerSecondRates) {
            for (int processingDelay : processingDelays) {
                System.out.printf("Starting test with rate: %d msg/s and delay: %d ms%n", messagesPerSecond, processingDelay);

                // Start producer for fixed duration
                producerController.startProducer(messagesPerSecond, topic, TEST_DURATION_MS);

                // Run consumer test with specific processing delay
                TestResult result = runConsumerTest(topic, processingDelay, consumerProperties, TEST_DURATION_MS);
                result.setMessagesPerSecond(messagesPerSecond);
                combinedReport.add(result);

                System.out.printf("Completed test for rate: %d msg/s and delay: %d ms%n", messagesPerSecond, processingDelay);
            }
        }

        // Print the final combined report
        System.out.println("\nFinal Combined Test Report:");
        printReport(combinedReport);
    }

    /**
     * Deletes all topics from the Kafka broker.
     */
    private static void deleteAllTopics(Properties properties) {
        try (AdminClient admin = AdminClient.create(properties)) {
            Set<String> topicNames = admin.listTopics().names().get(); // Fetch all topic names
            if (!topicNames.isEmpty()) {
                admin.deleteTopics(topicNames).all().get();
                System.out.printf("All topics deleted successfully.");
                Thread.sleep(5000); // Allow propagation time
            } else {
                System.out.printf("No topics found to delete.");
            }
        } catch (Exception e) {
            System.out.printf("Error occurred while deleting all topics: {}", e.getMessage());
        }
    }

    /**
     * Creates a Kafka topic with the specified number of partitions.
     */
    private static void createTopic(String topicName, int numPartitions, Properties properties) {
        try (AdminClient adminClient = AdminClient.create(properties)) {
            System.out.printf("Creating topic '%s' with %d partition(s)...%n", topicName, numPartitions);
            NewTopic newTopic = new NewTopic(topicName, numPartitions, (short) 1); // Replication factor = 1
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get(); // Wait for creation to complete
            System.out.printf("Topic '%s' created successfully.%n", topicName);
        } catch (Exception e) {
            System.err.printf("Error while creating topic '%s': %s%n", topicName, e.getMessage());
        }
    }

    /**
     * Wait for the Kafka topic to be ready.
     */
    private static void waitForTopicToBeReady(String topic, Properties properties) {
        try (AdminClient adminClient = AdminClient.create(properties)) {
            boolean isReady = false;
            while (!isReady) {
                var result = adminClient.describeTopics(Collections.singletonList(topic)).all();
                isReady = result.get().values().stream()
                        .allMatch(desc -> desc.partitions().stream()
                                .allMatch(partition -> partition.leader() != null));
                if (!isReady) {
                    System.out.printf("Waiting for topic '%s' to be ready...%n", topic);
                    Thread.sleep(1000); // Check readiness every second
                }
            }
            System.out.printf("Topic '%s' is ready.%n", topic);
        } catch (Exception e) {
            System.out.printf("Error while waiting for topic readiness: %s%n", e.getMessage());
        }
    }

    /**
     * Runs the consumer test for a specific processing delay.
     */
    private static TestResult runConsumerTest(String topic, int processingDelay, Properties consumerProperties, int durationMs) throws Exception {
        KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(consumerProperties);

        // Subscribe to topic
        consumer.subscribe(Collections.singletonList(topic));
        System.out.printf("Consumer subscribed to topic: %s%n", topic);

        // Metrics
        Map<TopicPartition, Long> latestOffsets = new HashMap<>();
        AtomicLong totalLag = new AtomicLong(0);
        AtomicLong totalMessagesProcessed = new AtomicLong(0);

        // Set strict test runtime duration
        final long endTestTime = System.currentTimeMillis() + durationMs;

        while (System.currentTimeMillis() < endTestTime) {
            ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(POLL_DURATION_MS));

            // Process records
            if (!records.isEmpty()) {
                long recordCount = records.count();
                totalMessagesProcessed.addAndGet(recordCount);

                // Fetch latest offsets and calculate current lag
                latestOffsets.putAll(consumer.endOffsets(consumer.assignment()));
                long currentLag = calculateCurrentLag(consumer, latestOffsets);
                totalLag.addAndGet(currentLag);

                // Process records with scaled delay
                for (ConsumerRecord<String, Object> record : records) {
                    Thread.sleep(processingDelay / Math.max(recordCount, 1)); // Distribute delay among records
                }

                // Commit offsets
                consumer.commitSync();
            }
        }

        consumer.close();

        // Compute result for this test run
        long averageLag = totalLag.get() / Math.max(totalMessagesProcessed.get(), 1); // Avoid divide-by-zero
        return new TestResult(processingDelay, totalMessagesProcessed.get(), averageLag, totalLag.get());
    }



    /**
     * Calculate consumer lag based on current offsets and latest end offsets in partitions.
     */
    private static long calculateCurrentLag(KafkaConsumer<String, Object> consumer,
                                            Map<TopicPartition, Long> latestOffsets) {
        long lag = 0;
        for (TopicPartition partition : consumer.assignment()) {
            long latestOffset = latestOffsets.getOrDefault(partition, 0L);
            long currentOffset = consumer.position(partition);
            lag += Math.max(latestOffset - currentOffset, 0); // Ensure no negative lag is calculated
        }
        return lag;
    }

    /**
     * Print the final combined test report.
     */
    private static void printReport(List<TestResult> report) {
        System.out.println("Messages/Sec | Processing Delay (ms) | Messages Processed | Avg Lag | Cumulative Lag");
        System.out.println("---------------------------------------------------------------------------");
        for (TestResult result : report) {
            System.out.printf("%13d | %21d | %19d | %7d | %14d%n",
                    result.messagesPerSecond, result.processingDelay, result.messagesProcessed,
                    result.averageLag, result.cumulativeLag);
        }
    }

    /**
     * Data structure to hold the results for each test run.
     */
    private static class TestResult {
        int messagesPerSecond;
        int processingDelay;
        long messagesProcessed;
        long averageLag;
        long cumulativeLag;

        public TestResult(int processingDelay, long messagesProcessed, long averageLag, long cumulativeLag) {
            this.processingDelay = processingDelay;
            this.messagesProcessed = messagesProcessed;
            this.averageLag = averageLag;
            this.cumulativeLag = cumulativeLag;
        }

        public void setMessagesPerSecond(int messagesPerSecond) {
            this.messagesPerSecond = messagesPerSecond;
        }
    }
}