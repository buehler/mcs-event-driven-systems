package com.experiments;

import com.data.Clicks;
import com.google.common.io.Resources;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.TopicExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.stats.*;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ProducerExperimentLoadTestClicksProducer {
    private static final Logger logger = LoggerFactory.getLogger(ProducerExperimentLoadTestClicksProducer.class);
    private static final int MAX_RETRIES = 50; // Retry limit for message sends

    static class ExperimentResult {
        int batchSize;
        int numProducers;
        long totalMessages;
        long durationMs;
        double throughput;
        double averageLatency;
        int messageDropRate;
        double avgCpu;
        double avgMemory;

        public ExperimentResult(int batchSize, int numProducers, long totalMessages, long durationMs,
                                double throughput, double averageLatency, int messageDropRate,
                                double avgCpu, double avgMemory) {
            this.batchSize = batchSize;
            this.numProducers = numProducers;
            this.totalMessages = totalMessages;
            this.durationMs = durationMs;
            this.throughput = throughput;
            this.averageLatency = averageLatency;
            this.messageDropRate = messageDropRate;
            this.avgCpu = avgCpu;
            this.avgMemory = avgMemory;
        }
    }

    public static void main(String[] args) throws Exception {
        Properties baseProperties = new Properties();

        // Load Kafka producer properties
        try (InputStream props = Resources.getResource("producer.properties").openStream()) {
            baseProperties.load(props);
            logger.info("Kafka producer base properties loaded successfully.");
        }

        String kafkaContainerName = "docker-kafka-1"; // Kafka Docker container
        int batchSize = 16384; // Fixed batch size
        int maxProducers = 5;
        boolean isMessagesPerProducer = false; // Total messages or messages per producer

        int totalMessageCount = 8000000; // Total messages to send per experiment

        List<ExperimentResult> results = new ArrayList<>();

        // Step 1: Delete all existing topics
        deleteAllTopics(baseProperties);

        // Step 2: Wait for Kafka to be ready
        waitForKafkaReadiness(kafkaContainerName);

        // Step 3: Create topics, verify readiness, and execute the experiment
        for (int numProducers = 1; numProducers <= maxProducers; numProducers++) {
            String topic = "click-events-load-test-" + UUID.randomUUID(); // Unique topic name for each test
            logger.info("Setting up experiment with {} producer(s) on topic '{}'.", numProducers, topic);

            // Create topic and wait for readiness
            createTopic(topic, baseProperties);
            waitForTopicToBeReady(topic, baseProperties);

            // Refresh producer metadata for the topic
            try (KafkaProducer<String, Clicks> tempProducer = new KafkaProducer<>(baseProperties)) {
                tempProducer.partitionsFor(topic); // Force metadata refresh
                logger.info("Producer metadata refreshed for topic '{}'.", topic);
            }

            // Perform the experiment
            ExperimentResult result = executeExperiment(
                    topic,
                    batchSize,
                    numProducers,
                    kafkaContainerName,
                    baseProperties,
                    totalMessageCount,
                    isMessagesPerProducer
            );
            results.add(result);
            Thread.sleep(2000); // Safety wait before reporting
        }

        // Step 4: Clean after experiment
        deleteAllTopics(baseProperties);

        // Step 5: Print the experiment results
        printReport(results);

    }

    /**
     * Deletes all topics from the Kafka broker.
     */
    private static void deleteAllTopics(Properties properties) {
        try (AdminClient admin = AdminClient.create(properties)) {
            Set<String> topicNames = admin.listTopics().names().get(); // Fetch all topic names
            if (!topicNames.isEmpty()) {
                admin.deleteTopics(topicNames).all().get();
                logger.info("All topics deleted successfully.");
                Thread.sleep(5000); // Allow propagation time
            } else {
                logger.info("No topics found to delete.");
            }
        } catch (Exception e) {
            logger.error("Error occurred while deleting all topics: {}", e.getMessage());
        }
    }

    /**
     * Waits for Kafka to reach readiness state.
     */
    private static void waitForKafkaReadiness(String kafkaContainerName) {
        try {
            boolean isReady = false;
            DockerStatsCollector dockerStats = new DockerStatsCollector();

            logger.info("Waiting for Kafka container '{}' to become ready...", kafkaContainerName);
            while (!isReady) {
                dockerStats.fetchStats(kafkaContainerName);
                double cpuUsage = dockerStats.getCpuUsage();
                double memoryUsage = dockerStats.getMemoryUsageMiB();

                logger.debug("Kafka Stats - CPU: {}%, Memory: {} MiB", cpuUsage, memoryUsage);
                isReady = cpuUsage > 0 && memoryUsage > 100; // Ensure Kafka is using resources
                if (!isReady) {
                    Thread.sleep(2000); // Poll every 2 seconds
                }
            }
            logger.info("Kafka container '{}' is ready.", kafkaContainerName);
        } catch (Exception e) {
            logger.error("Error while waiting for Kafka readiness: {}", e.getMessage());
        }
    }

    private static void waitForTopicToBeReady(String topic, Properties properties) {
        try (AdminClient admin = AdminClient.create(properties)) {
            boolean isReady = false;
            while (!isReady) {
                var result = admin.describeTopics(Collections.singletonList(topic)).all();
                isReady = result.get().values().stream()
                        .allMatch(desc -> desc.partitions().stream()
                                .allMatch(partition -> partition.leader() != null));
                if (!isReady) {
                    Thread.sleep(2000); // Polling interval of 2 seconds
                }
            }
            logger.info("Topic '{}' is ready.", topic);
        } catch (Exception e) {
            logger.error("Error while waiting for topic {} readiness: {}", topic, e.getMessage(), e);
        }
    }

    /**
     * Creates a topic in Kafka with the given properties.
     */
    private static void createTopic(String topic, Properties properties) {
        try (AdminClient admin = AdminClient.create(properties)) {
            NewTopic newTopic = new NewTopic(topic, 1, (short) 1); //
            admin.createTopics(Collections.singleton(newTopic)).all().get();
            logger.info("Topic '{}' created successfully.", topic);
        } catch (Exception e) {
            logger.error("Error creating topic '{}': {}", topic, e.getMessage());
        }
    }

    private static ExperimentResult executeExperiment(String topic, int batchSize, int numProducers,
                                                      String kafkaContainerName, Properties baseProperties,
                                                      int totalMessageCount, boolean isMessagesPerProducer) throws Exception {
        int messagesPerProducer = isMessagesPerProducer ?
                totalMessageCount : totalMessageCount / numProducers;

        AtomicLong totalLatency = new AtomicLong(0); // Tracks total latency in milliseconds
        AtomicInteger totalMessagesSent = new AtomicInteger(0); // Tracks successfully sent messages
        AtomicInteger totalFailures = new AtomicInteger(0); // Tracks failures

        DockerStatsCollector dockerStats = new DockerStatsCollector();
        ScheduledExecutorService statsExecutor = Executors.newScheduledThreadPool(1);
        AtomicLong cpuSum = new AtomicLong(0);
        AtomicLong memorySum = new AtomicLong(0);
        AtomicInteger samples = new AtomicInteger(0);

        statsExecutor.scheduleAtFixedRate(() -> {
            try {
                dockerStats.fetchStats(kafkaContainerName);
                cpuSum.addAndGet((long) dockerStats.getCpuUsage());
                memorySum.addAndGet((long) dockerStats.getMemoryUsageMiB());
                samples.incrementAndGet();
            } catch (Exception e) {
                logger.warn("Docker stats sampling encountered an error: {}", e.getMessage());
            }
        }, 0, 50, TimeUnit.MILLISECONDS); // 5-sec sampling interval

        ExecutorService producerExecutor = Executors.newFixedThreadPool(numProducers);
        long experimentStart = System.currentTimeMillis();

        for (int i = 0; i < numProducers; i++) {
            Properties producerProperties = (Properties) baseProperties.clone();
            producerProperties.put("batch.size", String.valueOf(batchSize));
            KafkaProducer<String, Clicks> producer = new KafkaProducer<>(producerProperties);

            final int producerIndex = i;
            producerExecutor.submit(() -> {
                for (int j = 0; j < messagesPerProducer; j++) {
                    Clicks clickEvent = new Clicks(j, System.nanoTime(), getRandomNumber(0, 1920), getRandomNumber(0, 1080), "EL" + getRandomNumber(1, 20));
                    long sendStart = System.nanoTime(); // Start latency timer
                    sendMessage(producer, topic, clickEvent, totalMessagesSent, totalLatency);                }
                logger.info("Producer {} completed.", producerIndex);
            });
        }

        producerExecutor.shutdown();
        producerExecutor.awaitTermination(15, TimeUnit.MINUTES);

        statsExecutor.shutdown();
        statsExecutor.awaitTermination(5, TimeUnit.SECONDS);

        long experimentEnd = System.currentTimeMillis();
        long duration = experimentEnd - experimentStart;
        double throughput = totalMessagesSent.get() / (duration / 1000.0);
        double averageLatency = totalLatency.get() / (double) totalMessagesSent.get();
        int messageDropRate = (numProducers * messagesPerProducer) - totalMessagesSent.get();
        double avgCpu = cpuSum.get() / (double) samples.get();
        double avgMemory = memorySum.get() / (double) samples.get();

        return new ExperimentResult(batchSize, numProducers, totalMessagesSent.get(),
                duration, throughput, averageLatency, messageDropRate, avgCpu, avgMemory);
    }

    private static void sendMessage(KafkaProducer<String, Clicks> producer, String topic, Clicks clickEvent,
                                    AtomicInteger totalMessagesSent, AtomicLong totalLatency) {
        long sendStart = System.nanoTime();
        producer.send(new ProducerRecord<>(topic, clickEvent), (metadata, exception) -> {
            if (exception == null) {
                long latency = System.nanoTime() - sendStart;
                totalLatency.addAndGet(TimeUnit.NANOSECONDS.toMillis(latency));
                totalMessagesSent.incrementAndGet();
                logger.debug("Latency for message: {} ms", TimeUnit.NANOSECONDS.toMillis(latency));
            } else {
                logger.error("Send failed for message {}: {}", clickEvent, exception.getMessage());
            }
        });
    }



    private static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    private static void printReport(List<ExperimentResult> results) {
        System.out.println("\n======= Experiment Results =======");
        System.out.printf("%-15s %-15s %-15s %-20s %-20s %-20s %-15s %-15s %-15s %-15s\n",
                "Batch Size", "Producers", "Messages Sent", "Duration (ms)", "Throughput (msg/sec)",
                "Avg Latency (ms)", "Message Drop", "Dropped (%)", "Avg CPU (%)", "Avg Memory (MB)");

        for (ExperimentResult result : results) {
            // Calculate the percentage of messages dropped
            double dropPercentage = (double) result.messageDropRate /
                    (result.totalMessages + result.messageDropRate) * 100;

            System.out.printf("%-15d %-15d %-15d %-20d %-20.2f %-20.2f %-15d %-15.2f %-15.2f %-15.2f\n",
                    result.batchSize, result.numProducers, result.totalMessages,
                    result.durationMs, result.throughput, result.averageLatency,
                    result.messageDropRate, dropPercentage, result.avgCpu, result.avgMemory);
        }
    }
}