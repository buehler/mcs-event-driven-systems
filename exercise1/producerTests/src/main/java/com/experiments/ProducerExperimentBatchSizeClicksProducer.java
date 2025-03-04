package com.experiments;

import com.data.Clicks;
import com.google.common.io.Resources;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class ProducerExperimentBatchSizeClicksProducer {

    private static final Logger logger = LoggerFactory.getLogger(ProducerExperimentBatchSizeClicksProducer.class);

    static class ExperimentResult {
        int batchSize;
        long totalMessages;
        long durationMs;
        double throughput;
        double averageLatency;

        public ExperimentResult(int batchSize, long totalMessages, long durationMs, double throughput, double averageLatency) {
            this.batchSize = batchSize;
            this.totalMessages = totalMessages;
            this.durationMs = durationMs;
            this.throughput = throughput;
            this.averageLatency = averageLatency;
        }
    }

    public static void main(String[] args) throws Exception {
        String topic = "click-events";
        List<Integer> batchSizes = List.of(1024, 4096, 16384, 65536, 262144, 1048576); // Small, Medium, Large batch sizes
        List<ExperimentResult> results = new ArrayList<>();     // List to store results for each batch size

        Properties baseProperties = new Properties();
        try (InputStream props = Resources.getResource("producer.properties").openStream()) {
            baseProperties.load(props);
            logger.info("Kafka producer base properties loaded successfully.");
        }

        for (int batchSize : batchSizes) {
            logger.info("Starting test for batch.size = {} bytes", batchSize);

            Properties experimentProperties = (Properties) baseProperties.clone();
            experimentProperties.put("batch.size", String.valueOf(batchSize));
            experimentProperties.put("linger.ms", "5"); // Fixed linger.ms for consistency
            experimentProperties.put("buffer.memory", "33554432"); // Default buffer memory (32MB)
            KafkaProducer<String, Clicks> producer = new KafkaProducer<>(experimentProperties);

            deleteTopicIfExists(topic, baseProperties);
            createTopicIfNotExists(topic, 1, experimentProperties);

            ExperimentResult result = runExperiment(producer, topic, batchSize);
            results.add(result);

            producer.close();
            logger.info("Completed test for batch.size = {}", batchSize);
        }

        printReport(results);
    }

    private static ExperimentResult runExperiment(KafkaProducer<String, Clicks> producer, String topic, int batchSize) {
        int totalMessages = 50000;                     // Total messages to send in each experiment
        java.util.concurrent.atomic.AtomicLong totalLatency = new java.util.concurrent.atomic.AtomicLong(0); // Mutable total latency
        java.util.concurrent.atomic.AtomicInteger messagesSent = new java.util.concurrent.atomic.AtomicInteger(0); // Mutable message count


        logger.info("Sending {} messages to topic '{}' with batch.size = {} bytes", totalMessages, topic, batchSize);

        // Start experiment
        long experimentStart = System.currentTimeMillis(); // Experiment start time
        for (int i = 0; i < totalMessages; i++) {
            // Generate random click event
            Clicks clickEvent = new Clicks(
                    i,                          // eventID
                    System.nanoTime(),          // timestamp
                    getRandomNumber(0, 1920),   // xPosition
                    getRandomNumber(0, 1080),   // yPosition
                    "EL" + getRandomNumber(1, 20) // clickedElement
            );

            // Record send start time
            long sendStart = System.nanoTime();
            producer.send(new ProducerRecord<>(topic, clickEvent), (metadata, exception) -> {
                if (exception == null) {
                    long latency = System.nanoTime() - sendStart;
                    totalLatency.addAndGet(TimeUnit.NANOSECONDS.toMillis(latency));
                    messagesSent.incrementAndGet();
                    logger.debug("Message sent to topic {}, Partition {}, Offset {}, Latency {} ns",
                            metadata.topic(), metadata.partition(), metadata.offset(), latency);
                } else {
                    logger.error("Error while sending message for event: {}", clickEvent, exception);
                }
            });
        }

        producer.flush();

        long experimentDuration = System.currentTimeMillis() - experimentStart; // Total experiment time
        double throughput = (double) messagesSent.get() / (experimentDuration / 1000.0); // Messages per second
        double averageLatency = (double) totalLatency.get() / messagesSent.get(); // Average latency per message

        logger.info("Results for batch.size = {} bytes: Total Messages = {}, Duration = {} ms, Throughput = {} msgs/sec, Average Latency = {} ms",
                batchSize, messagesSent.get(), experimentDuration, throughput, averageLatency);

        return new ExperimentResult(batchSize, messagesSent.get(), experimentDuration, throughput, averageLatency);
    }

    private static void createTopicIfNotExists(String topicName, int numPartitions, Properties properties) throws Exception {
        AdminClient admin = AdminClient.create(properties);
        boolean topicExists = admin.listTopics().names().get().contains(topicName);
        if (!topicExists) {
            NewTopic topic = new NewTopic(topicName, numPartitions, (short) 1);
            admin.createTopics(Collections.singletonList(topic));
            logger.info("Created topic: {}", topicName);
        } else {
            logger.info("Topic already exists: {}", topicName);
        }
    }

    private static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    private static void deleteTopicIfExists(String topicName, Properties properties) throws Exception {
        AdminClient admin = AdminClient.create(properties);
        boolean topicExists = admin.listTopics().names().get().contains(topicName);
        if (topicExists) {
            admin.deleteTopics(Collections.singletonList(topicName)).all().get();
            logger.info("Deleted existing topic: {}", topicName);
        } else {
            logger.info("Topic does not exist: {}", topicName);
        }
    }

    private static void printReport(List<ExperimentResult> results) {
        System.out.println("\n======= Experiment Results =======");
        System.out.printf("%-15s %-15s %-15s %-15s %-15s\n",
                "Batch Size", "Messages", "Duration (ms)", "Throughput", "Avg Latency (ms)");
        System.out.println("---------------------------------------------------------------");

        for (ExperimentResult result : results) {
            System.out.printf("%-15d %-15d %-15d %-15.2f %-15.2f\n",
                    result.batchSize,
                    result.totalMessages,
                    result.durationMs,
                    result.throughput,
                    result.averageLatency);
        }
        System.out.println("==================================\n");
    }
}