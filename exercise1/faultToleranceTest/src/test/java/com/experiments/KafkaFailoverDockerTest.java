package com.experiments;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KafkaFailoverDockerTest {
    private static final String fileTimestamp;
    static {
        // Dynamically set the current.date property for the log file name
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        fileTimestamp = dateFormat.format(new Date());
        System.setProperty("current.date", fileTimestamp);
    }


    private static final Logger logger = LoggerFactory.getLogger(KafkaFailoverDockerTest.class);

    private final String topic = "test-replication-topic"; // Test topic name
    private KafkaProducer<String, String> producer;
    private AdminClient adminClient;
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());

    // Shutdown signaling mechanism
    private final AtomicBoolean running = new AtomicBoolean(true);

    // Test metrics
    private final Map<String, Object> testMetrics = new LinkedHashMap<>();
    private int totalProducedMessages = 0;
    private int totalConsumedMessages = 0;

    // Counters for producer failures and consumer rebalances
    private final AtomicInteger failedProducerCount = new AtomicInteger();
    private final AtomicInteger rebalanceCount = new AtomicInteger();

    // Counters for tracking partition reassignments during consumer rebalance
    private final AtomicInteger revokedPartitionsCount = new AtomicInteger();
    private final AtomicInteger assignedPartitionsCount = new AtomicInteger();

    // Node failure tracking
    private final AtomicLong lastProducedBeforeNodeFailure = new AtomicLong(0);
    private final AtomicLong firstProducedAfterNodeFailure = new AtomicLong(0);
    private final AtomicLong lastConsumedBeforeNodeFailure = new AtomicLong(0);
    private final AtomicLong firstConsumedAfterNodeFailure = new AtomicLong(0);

    // Node recovery tracking
    private final AtomicLong lastProducedBeforeNodeRecovery = new AtomicLong(0);
    private final AtomicLong firstProducedAfterNodeRecovery = new AtomicLong(0);
    private final AtomicLong lastConsumedBeforeNodeRecovery = new AtomicLong(0);
    private final AtomicLong firstConsumedAfterNodeRecovery = new AtomicLong(0);

    private final AtomicBoolean isRecovering = new AtomicBoolean(false);
    private final AtomicBoolean isFailing = new AtomicBoolean(false);


    @BeforeAll
    public void setup() throws Exception {
        setupAdminClient();


        // Delete all topics and wait for deletion to complete
        deleteAllTopics();

        // Verify deletion before creating the topic
        Set<String> existingTopics = adminClient.listTopics().names().get();
        if (existingTopics.contains(topic)) {
            throw new IllegalStateException("Topic is still marked for deletion: " + topic);
        }

        // Create the test topic with 3 partitions and replication factor of 3
        createTopic(topic, 3, 3);

        // Wait for  seconds for topic metadata to propagate
        System.out.println("Waiting for topic metadata to propagate...");
        Thread.sleep(10000); // Adjust this time if necessary

        // Initialize test metrics
        testMetrics.put("Test Name", "Kafka Broker Failover Test");
        testMetrics.put("Topic Name", topic);
    }

    @AfterAll
    public void cleanup() throws IOException {
        try {
            if (producer != null) producer.close(Duration.ofSeconds(30)); // Graceful producer shutdown
            if (adminClient != null) adminClient.close();
        } finally {
            // Print final test metrics report

            printTestReport();
        }
    }

    @Test
    public void testKafkaFailover() throws Exception {
        List<Thread> producerThreads = new ArrayList<>();
        // Create a "data" marker
        Marker dataMarker = MarkerFactory.getMarker("DATA");
//        logger.info(dataMarker, String.join(";",
//                "eventType",
//                "event",
//                "threadNumber",
//                "value",
//                "partition",
//                "offset"
//        ));




        // Create and start multiple producer threads
        for (int i = 1; i <= 3; i++) {
            int threadNumber = i; // For thread identification
            Thread producerThread = new Thread(() -> {


                // Each thread will have a separate producer
                try (KafkaProducer<String, String> producer = createProducer()) {
                    startProducingMessages(producer, timestamp -> {
                    });


                } catch (Exception e) {
                    System.err.printf("Exception in Producer Thread-%d: %s%n", threadNumber, e.getMessage());
                }
            });

            producerThread.setName("Producer-Thread-" + i);
//            logger.info(dataMarker, String.join(";",
//                    "event",
//                    "Producer thread started",
//                    String.valueOf(threadNumber),
//                    "-",
//                    "-",
//                    "-"
//            ));
            producerThread.start();
            // Add thread to the list for later management
            producerThreads.add(producerThread);
        }

        // Start consuming messages with tracking
        Thread consumerThread = new Thread(() -> startConsumingMessages(timestamp -> {
        }));
//        logger.info(dataMarker, String.join(";",
//                "event",
//                "Consumer thread started",
//                "4",
//                "-",
//                "-",
//                "-"
//        ));
        consumerThread.start();

        logger.info(dataMarker, String.join(";",
                "event",
                "start",
                "-",
                "-",
                "-"
        ));

        // Stabilize for 10 seconds 10000 ms
        Thread.sleep(10000);

        // Simulate broker failure
        firstProducedAfterNodeFailure.set(0);
        firstConsumedAfterNodeFailure.set(0);
        isRecovering.set(false);
        long killStartTime = System.currentTimeMillis();
        killBroker("kafka1");
        long timeToKillBroker = System.currentTimeMillis() - killStartTime;
        testMetrics.put("Broker Kill Time (ms)", timeToKillBroker);
        System.out.println("Broker kafka1 stopped. Observing failover...");

        // Measure leader election time during failover
        long failoverStartTime = System.currentTimeMillis();
        boolean allLeadersElected = waitForLeaderElection();
        long failoverTime = System.currentTimeMillis() - failoverStartTime;
        logger.info(dataMarker, String.join(";",
                "event",
                "Leader Election Completed",
                String.valueOf(failoverTime), // value
                "-", // partition
                "-" // offset
        ));

        testMetrics.put("Leader Election Time (ms)", failoverTime);

        assertTrue(allLeadersElected, "Leader election did not complete successfully");

        // Wait for 30 seconds for cluster recovery
        Thread.sleep(10000);

        // Restart the failed broker
        firstProducedAfterNodeRecovery.set(0);
        firstConsumedAfterNodeRecovery.set(0);
        long restartStartTime = System.currentTimeMillis();
        restartBroker("kafka1");
        long restartTime = System.currentTimeMillis() - restartStartTime;
        testMetrics.put("Broker Restart Time (ms)", restartTime);
        System.out.println("Broker kafka1 restarted.");

        // Wait for 10 seconds for cluster recovery
        Thread.sleep(10000);

        // Stop threads gracefully
        System.out.println("Stopping producer and consumer threads...");
        running.set(false);          // Signal threads to stop
        // Join all producer threads
        for (Thread producerThread : producerThreads) {
            producerThread.join(3000); // Wait for each producer thread to stop
        }
        consumerThread.join(3000);       // Wait for consumer thread to stop
        logger.info(dataMarker, String.join(";",
                "event",
                "end",
                "-",
                "-",
                "-"
        ));
        System.out.println("Producer and consumer threads stopped.");

        // Calculate producer-consumer lag
        long producerLag = totalProducedMessages - totalConsumedMessages;
        testMetrics.put("Consumer Lag (Messages)", totalConsumedMessages > 0 ? producerLag : "No messages consumed");


        // Record produced and consumed message counts
        testMetrics.put("Total Produced Messages", totalProducedMessages);
        testMetrics.put("Total Consumed Messages", totalConsumedMessages);
        testMetrics.put("Test Successful", allLeadersElected && totalConsumedMessages > 0);


        testMetrics.put("Revoked Partitions Count", revokedPartitionsCount.get());
        testMetrics.put("Assigned Partitions Count", assignedPartitionsCount.get());
    }

    /**
     * Converts a millisecond timestamp into a human-readable format.
     *
     * @param timestamp the timestamp in milliseconds
     * @return the formatted timestamp as a string
     */
    private String formatTimestamp(long timestamp) {
        if (timestamp == 0) {
            return "N/A"; // Handle uninitialized timestamps
        }
        return TIMESTAMP_FORMATTER.format(Instant.ofEpochMilli(timestamp));
    }



    /** Delete all topics using Kafka AdminClient */
    private void deleteAllTopics() throws ExecutionException, InterruptedException {
        System.out.println("Deleting all existing topics...");

        // Fetch the existing topic names
        Set<String> topicNames = adminClient.listTopics().names().get();

        if (topicNames.isEmpty()) {
            System.out.println("No topics found to delete.");
            return;
        }

        // Delete all topics
        adminClient.deleteTopics(topicNames).all().get();
        System.out.printf("Deleted topics: %s%n", topicNames);

        // Wait for all topics to be deleted
        int retries = 30; // Max wait time: 30 seconds
        while (retries-- > 0) {
            Set<String> remainingTopics = adminClient.listTopics().names().get();

            if (remainingTopics.isEmpty()) {
                System.out.println("Waiting for topics do be deleted...");
                Thread.sleep(5000); // Adjust this time if necessary
                System.out.println("All topics deleted successfully.");
                return;
            }

            if (remainingTopics.stream().anyMatch(topic -> topic.startsWith("__"))) {
                System.out.println("Kafka system topics remain, but test-specific topics are deleted.");
                return;
            }

            System.out.println("Waiting for all topics to be fully deleted...");
            Thread.sleep(1000);
        }

        throw new IllegalStateException("Failed to delete all topics within timeout. Some topics may still be marked for deletion.");
    }

    /** Create a Kafka topic */
    private void createTopic(String topicName, int partitions, int replicationFactor) throws ExecutionException, InterruptedException {
        NewTopic newTopic = new NewTopic(topicName, partitions, (short) replicationFactor);
        adminClient.createTopics(Collections.singleton(newTopic)).all().get();
        System.out.printf("Topic [%s] created with %d partitions and replication factor %d.%n", topicName, partitions, replicationFactor);
    }


    /** Stop a Kafka broker using Docker */
    private void killBroker(String brokerName) throws Exception {
        Marker dataMarker = MarkerFactory.getMarker("DATA");

        System.out.printf("Stopping broker [%s]...%n", brokerName);
        Runtime.getRuntime().exec(String.format("docker stop %s", brokerName));
        logger.info(dataMarker, String.join(";",
                "event",
                "kafka1 broker stopped",
                "-",
                "-",
                "-"
        ));
        isFailing.set(true);
    }

    /** Restart a Kafka broker */
    private void restartBroker(String brokerName) throws Exception {
        Marker dataMarker = MarkerFactory.getMarker("DATA");
        System.out.printf("\nRestarting broker '%s'...\n", brokerName);

        // Dynamically get the current working directory
        String currentDirectory = System.getProperty("user.dir");

        // Construct the relative path based on the current directory
        File workingDirectory = new File(currentDirectory, "../docker");

        // Example: Printing the constructed path for debugging
        System.out.println("Working Directory: " + workingDirectory.getAbsolutePath());

        ProcessBuilder processBuilder = new ProcessBuilder("docker-compose", "restart", brokerName);
        processBuilder.directory(workingDirectory); // Set the working directory
        processBuilder.redirectErrorStream(true); // Combine stderr and stdout

        // Start the process
        Process process = processBuilder.start();
        isRecovering.set(true);

        // Capture and print the output
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                System.out.println(line);
            }
        }

        // Wait for the process to complete and check its exit code
        int exitCode = process.waitFor();
        if (exitCode == 0) {
            System.out.printf("Broker '%s' restarted successfully.\n", brokerName);
            logger.info(dataMarker, String.join(";",
                    "event",
                    "kafka1 broker restarted",
                    "-",
                    "-",
                    "-"
            ));
        } else {
            throw new IllegalStateException(String.format("Failed to restart broker '%s'. Exit code: %d\nOutput: %s", brokerName, exitCode, output));
        }
    }

    /** Wait for leader election to finish and log partition changes */
    private boolean waitForLeaderElection() throws InterruptedException, ExecutionException {
        Marker dataMarker = MarkerFactory.getMarker("DATA");
        final long timeout = System.currentTimeMillis() + 30000; // 30 seconds max timeout
        Map<Integer, String> currentLeaders = new HashMap<>();

        while (System.currentTimeMillis() < timeout) {
            Map<String, TopicDescription> topicDescriptions = getTopicDescriptions();

            boolean noPartitionsHaveLeader1 = true; // Flag to detect if leader 1 is active
            boolean allPartitionsHaveLeaders = true; // Flag to ensure all partitions have leaders

            // Iterate over all partitions and check their current leaders
            for (TopicPartitionInfo partitionInfo : topicDescriptions.get(topic).partitions()) {
                int partition = partitionInfo.partition();
                String currentLeader = partitionInfo.leader() != null ? partitionInfo.leader().idString() : "none";

                // Keep track of the current leader for this partition
                currentLeaders.put(partition, currentLeader);

                // Check if any partition still has broker 1 as its leader
                if ("1".equals(currentLeader)) {
                    noPartitionsHaveLeader1 = false; // At least one partition still has leader 1
                }

                // Check if any partition currently has no leader
                if ("none".equals(currentLeader)) {
                    allPartitionsHaveLeaders = false; // At least one partition lacks a leader
                }
            }

            // If no partition has leader 1 and all have valid leaders, print the status and exit
            if (noPartitionsHaveLeader1 && allPartitionsHaveLeaders) {
                System.out.println("All partitions have transitioned to new leaders:");
                currentLeaders.forEach((partition, leader) -> {
                    System.out.printf("Partition %d: New leader = %s%n", partition, leader);
                }
                );

                return true;
            }


        }

        // If timeout occurs and conditions are not met
        System.err.println("Leader election timed out. Either some partitions still have leader 1 or leaders are missing.");
        return false;
    }


    /** Start producing messages with a tracking callback */
    private void startProducingMessages(KafkaProducer<String, String> producer, java.util.function.LongConsumer trackingCallback) {
        try {
            int key = 0;

            while (running.get() && !Thread.currentThread().isInterrupted()) {
                String value = "Message-" + key;
                String messageKey = Integer.toString(key); // Optional: Use this key for Kafka's default partitioning
                long startTime = System.currentTimeMillis();

                producer.send(new ProducerRecord<>(topic, messageKey, value), (metadata, exception) -> {
                    if (exception == null) {
                        // Get current thread name
                        String threadName = Thread.currentThread().getName();
                        String threadNumber = threadName.replaceAll(".*-(\\d+)$", "$1");

                        long elapsedTime = System.currentTimeMillis() - startTime;

                        // Log for graph
                        Marker dataMarker = MarkerFactory.getMarker("DATA");
                        logger.info(dataMarker, String.join(";",
                                "producer",
                                threadNumber,                                value,
                                String.valueOf(metadata.partition()),
                                String.valueOf(metadata.offset())
                        ));

//                        System.out.printf(
//                                "Produced: %s in %d ms (partition=%s, offset=%s, startTime=%d)%n",
//                                value,
//                                elapsedTime,
//                                metadata.partition(),
//                                metadata.offset(),
//                                startTime
//                        );


                        if (!isFailing.get() && !isRecovering.get()) {
                            lastProducedBeforeNodeFailure.set(startTime);
                        //    System.out.println("lastProducedBeforeNodeFailure set to: " + startTime);
                        } else {
                            // After leader election completed
                            if (isFailing.get() && !isRecovering.get() && firstProducedAfterNodeFailure.get() == 0) {
                                // Track the first successfully produced message *after* failover
                                firstProducedAfterNodeFailure.compareAndSet(0, startTime);
                                System.out.println("firstProducedAfterNodeFailure set to: " + startTime);                            }
                        }

                        if (!isRecovering.get() && isFailing.get()) {
                            lastProducedBeforeNodeRecovery.set(startTime); // Set once
                        //    System.out.println("lastProducedBeforeNodeRecovery set to: " + startTime);
                        } else {
                            if (isRecovering.get() && isFailing.get() && firstProducedAfterNodeRecovery.get() == 0) {
                                // Track the first successfully produced message after recovery
                                firstProducedAfterNodeRecovery.compareAndSet(0, startTime);
                                System.out.println("firstProducedAfterNodeRecovery set to: " + startTime);
                            }
                        }



                        // Tracking callback for additional monitoring
                        trackingCallback.accept(startTime);
                    } else {
                        synchronized (System.err) {
                            System.err.printf("Failed to produce message: %s - %s%n", value, exception.getMessage());
                        }
                        failedProducerCount.incrementAndGet();
                    }
                });

                totalProducedMessages++;
                Thread.sleep(40);
                key++;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            synchronized (System.out) {
                System.out.println("Producer thread interrupted.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            synchronized (System.out) {
                System.out.println("Producer thread stopped.");
            }
        }
    }

    /**
     * Start consuming messages with a tracking callback.
     */
    private void startConsumingMessages(java.util.function.LongConsumer trackingCallback) {
        try (KafkaConsumer<String, String> consumer = createConsumer()) {

            // Subscribe to the topic with a rebalance listener
            consumer.subscribe(Collections.singleton(topic), new ConsumerRebalanceListener() {
                @Override
                public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                    synchronized (System.out) {
                        System.out.println("Partitions revoked. Preparing for reassignment: " + partitions);
                    }
                    revokedPartitionsCount.addAndGet(partitions.size());
                }

                @Override
                public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                    synchronized (System.out) {
                        System.out.println("Partitions assigned after rebalance: " + partitions);
                    }
                    assignedPartitionsCount.addAndGet(partitions.size());
                }
            });

            synchronized (System.out) {
                System.out.println("Subscribed to topic: " + topic);
            }

            // Poll messages while running
            while (running.get()) {
                try {
                    // Poll records from the topic
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                    final long pollStartTime = System.currentTimeMillis();

                    // Get current thread name
                    String threadName = Thread.currentThread().getName();
                    String threadNumber = threadName.replaceAll(".*-(\\d+)$", "$1");

                    // Log for graph
                    Marker dataMarker = MarkerFactory.getMarker("DATA");


                    // Use a map to track messages per partition
                    Map<Integer, Integer> partitionMessageCount = new HashMap<>();
                    for (ConsumerRecord<String, String> record : records) {
                        long processingLatency = System.currentTimeMillis() - pollStartTime;

                        // Increment message count for each partition
                        partitionMessageCount.merge(record.partition(), 1, Integer::sum);

                        // Print consumed message details
                        logger.info(dataMarker, String.join(";",
                                "consumer",
                                threadNumber,
                                record.value(),
                                String.valueOf(record.partition()),
                                String.valueOf(record.offset())
                        ));


//                        synchronized (System.out) {
//                            System.out.printf(
//                                    "Consumed: key=%s, value=%s, partition=%d, offset=%d, latency=%d ms, pollStartTime=%s%n",
//                                    record.key(),
//                                    record.value(),
//                                    record.partition(),
//                                    record.offset(),
//                                    processingLatency,
//                                    pollStartTime
//                            );
//                        }

                        // Tracking callback with message timestamp
                        trackingCallback.accept(pollStartTime);

                        // Handle failover and recovery tracking
                        trackConsumptionState(pollStartTime);

                        // Increment total consumed count
                        totalConsumedMessages++;
                    }

                    // Commit offsets after processing
                    commitOffsets(consumer);
                } catch (Exception e) {
                    handleConsumptionError(e);
                }
            }
        } catch (Exception e) {
            handleConsumerCreationError(e);
        }
    }

    /** Track consumption timestamps for failover/recovery */
    private void trackConsumptionState(long pollStartTime) {
        if (!isFailing.get() && !isRecovering.get()) {
            // During failure: Track the last successfully produced message before failure
            lastConsumedBeforeNodeFailure.set(pollStartTime); // Set once
        //    System.out.println("lastConsumedBeforeNodeFailure set to: " + lastConsumedBeforeNodeFailure);

        } else {
            // After leader election completed
            if (isFailing.get() && !isRecovering.get() && firstConsumedAfterNodeFailure.get() == 0) {
                // Track the first successfully consumed message *after* failover
                firstConsumedAfterNodeFailure.compareAndSet(0, pollStartTime);
                System.out.println("firstConsumedAfterNodeFailure set to: " + firstConsumedAfterNodeFailure);

            }
        }

        if (!isRecovering.get() && isFailing.get()) {
            // During failure: Track the last successfully consumed message before failure
            lastConsumedBeforeNodeRecovery.set(pollStartTime); // Set once
        //    System.out.println("lastConsumedBeforeNodeRecovery set to: " + lastConsumedBeforeNodeRecovery);

        } else {
            // After leader election completed
            if (isRecovering.get() && isFailing.get() && firstConsumedAfterNodeRecovery.get() == 0) {
                // Track the first successfully consumed message *after* failover
                firstConsumedAfterNodeRecovery.compareAndSet(0, pollStartTime);
                System.out.println("firstConsumedAfterNodeRecovery set to: " + firstConsumedAfterNodeRecovery);
            }
        }

    }

    /** Commit consumer offsets with proper error handling */
    private void commitOffsets(KafkaConsumer<String, String> consumer) {
        try {
            consumer.commitSync();
        } catch (Exception ex) {
            synchronized (System.err) {
                System.err.printf("Failed to commit offsets: %s%n", ex.getMessage());
            }
            ex.printStackTrace();
        }
    }

    /** Handle errors during message consumption */
    private void handleConsumptionError(Exception e) {
        synchronized (System.err) {
            System.err.printf("Error while consuming messages: %s%n", e.getMessage());
        }
        e.printStackTrace();
    }

    /** Handle errors during consumer creation or setup */
    private void handleConsumerCreationError(Exception e) {
        synchronized (System.err) {
            System.err.printf("Error while creating or configuring the consumer: %s%n", e.getMessage());
        }
        e.printStackTrace();
    }


    // Kafka setup method (returns a producer instance)
    public KafkaProducer<String, String> createProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "130.82.26.193:9092,130.82.26.193:9093,130.82.26.193:9094");
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());
        props.put("acks", "all"); // Wait for all replicas to acknowledge
        props.put("retries", 3); // Limit retry attempts during failover
        props.put("retry.backoff.ms", 500); // Delay between retries
        props.put("request.timeout.ms", 15000); // Adjust timeout for metadata requests
        props.put("metadata.max.age.ms", 1000); // Frequent metadata refresh for failover scenarios
        props.put("linger.ms", 5); // Small delay to batch records for improved performance
        props.put("max.in.flight.requests.per.connection", 5); // Avoid message reordering during retry
        props.put("delivery.timeout.ms", 30000); // Total time to retry sending message (default is 2 minutes)
        props.put("reconnect.backoff.ms", 500); // Initial delay between retries
        props.put("reconnect.backoff.max.ms", 10000); // Maximum backoff time between retries
        return new KafkaProducer<>(props);
    }

    private KafkaConsumer<String, String> createConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "130.82.26.193:9092,130.82.26.193:9093,130.82.26.193:9094");
        props.put("group.id", "test-consumer-group");
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        props.put("auto.offset.reset", "latest"); // Start from the latest offsets
        props.put("enable.auto.commit", "false"); // Disable auto commit for manual offset management
        props.put("max.poll.interval.ms", 60000); // Extend poll interval for longer processing times
        props.put("fetch.max.wait.ms", 500); // Max wait time for fetching records
        props.put("max.poll.records", 500); // Limit the number of records returned per poll
        props.put("session.timeout.ms", 10000); // Give enough time for failover recovery
        props.put("heartbeat.interval.ms", 1000); // Ensure heartbeats match session timeout
        props.put("request.timeout.ms", 40000); // Timeout for consumer-broker communication during recovery
        //props.put("max.partition.fetch.bytes", 1048576); // Ensure proper batch sizes
        props.put("fetch.min.bytes", 1); // Ensure low minimum fetch size for prompt broker availability checks
        props.put("reconnect.backoff.ms", 1000); // Delay before reconnect
        props.put("reconnect.backoff.max.ms", 10000); // Maximum backoff for reconnect attempts
        return new KafkaConsumer<>(props);
    }


    public void setupAdminClient() {
        Properties props = new Properties();
        // Set correct address here
        props.put("bootstrap.servers", "130.82.26.193:9092,130.82.26.193:9093,130.82.26.193:9094");
        adminClient = AdminClient.create(props);
    }

    /** Fetch topic details using Kafka AdminClient */
    private Map<String, TopicDescription> getTopicDescriptions() throws ExecutionException, InterruptedException {
        return adminClient.describeTopics(Collections.singleton(topic)).all().get();
    }



    /** Print the test report */
    private void printTestReport() {
        System.out.println("\n========== Kafka Failover Test Report ==========");
        testMetrics.forEach((key, value) -> System.out.printf("%s: %s%n", key, value));

        if (lastProducedBeforeNodeFailure.get() > 0 && firstProducedAfterNodeFailure.get() > 0) {
            long producerFailoverRecoveryTime = firstProducedAfterNodeFailure.get() - lastProducedBeforeNodeFailure.get();
            testMetrics.put("Producer Recovery Time (Node Failure)", producerFailoverRecoveryTime);
        }
        System.out.println("Producer Recovery Time (Node Failure): " +
                testMetrics.get("Producer Recovery Time (Node Failure)") + " ms");

        if (lastConsumedBeforeNodeFailure.get() > 0 && firstConsumedAfterNodeFailure.get() > 0) {
            long consumerFailoverRecoveryTime = firstConsumedAfterNodeFailure.get() - lastConsumedBeforeNodeFailure.get();
            testMetrics.put("Consumer Recovery Time (Node Failure)", consumerFailoverRecoveryTime);
        }
        System.out.println("Consumer Recovery Time (Node Failure): " +
                testMetrics.get("Consumer Recovery Time (Node Failure)") + " ms");

        if (lastProducedBeforeNodeRecovery.get() > 0 && firstProducedAfterNodeRecovery.get() > 0) {
            long producerRecoveryRecoveryTime = firstProducedAfterNodeRecovery.get() - lastProducedBeforeNodeRecovery.get();
            testMetrics.put("Producer Recovery Time (Node Recovery)", producerRecoveryRecoveryTime);
        }
        System.out.println("Producer Recovery Time (Node Recovery): " +
                testMetrics.get("Producer Recovery Time (Node Recovery)") + " ms");

        if (lastConsumedBeforeNodeRecovery.get() > 0 && firstConsumedAfterNodeRecovery.get() > 0) {
            long consumerRecoveryRecoveryTime = firstConsumedAfterNodeRecovery.get() - lastConsumedBeforeNodeRecovery.get();
            testMetrics.put("Consumer Recovery Time (Node Recovery)", consumerRecoveryRecoveryTime);
        }
        System.out.println("Consumer Recovery Time (Node Recovery): " +
                testMetrics.get("Consumer Recovery Time (Node Recovery)") + " ms");


        System.out.println("===============================================");
    }

}