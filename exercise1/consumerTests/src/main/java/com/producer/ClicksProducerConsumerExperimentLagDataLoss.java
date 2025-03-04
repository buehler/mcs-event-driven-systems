package com.producer;

import com.producer.data.Clicks;
import com.google.common.io.Resources;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ClicksProducerConsumerExperimentLagDataLoss {

    private KafkaProducer<String, Clicks> producer;
    private ExecutorService executorService;
    private volatile boolean running = false; // Flag to determine if the producer is running

    /**
     * Starts producing messages at the given rate (messages/s) to the specified topic.
     *
     * @param messagesPerSecond Number of messages to send per second.
     * @param topic             Kafka topic to produce messages to.
     * @param durationMs        Duration to run the producer in milliseconds.
     */
    public void startProducer(int messagesPerSecond, String topic, int durationMs) {
        try {
            // Load Kafka producer properties
            Properties properties = new Properties();
            try (InputStream props = Resources.getResource("producer.properties").openStream()) {
                properties.load(props);
            }

            // Create the Kafka producer
            producer = new KafkaProducer<>(properties);

            // Initialize thread pool for producing messages
            executorService = Executors.newSingleThreadExecutor();
            running = true;

            // Submit task to produce messages
            executorService.submit(() -> {
                System.out.printf("Producer started: Sending %d messages/s to topic '%s' for %d ms.%n",
                        messagesPerSecond, topic, durationMs);

                int counter = 0; // EventID counter
                long interval = 1000L / messagesPerSecond; // Interval between messages in milliseconds
                long startTime = System.currentTimeMillis(); // Mark the start time

                while (running && (System.currentTimeMillis() - startTime < durationMs)) {
                    try {
                        // Generate and send a Click event
                        Clicks clickEvent = new Clicks(
                                counter++, // Event ID
                                System.nanoTime(), // Timestamp
                                getRandomNumber(0, 1920), // X Position
                                getRandomNumber(0, 1080), // Y Position
                                "EL" + getRandomNumber(1, 20) // Clicked Element
                        );
                        producer.send(new ProducerRecord<>(topic, clickEvent));
                        System.out.println("Sent: " + clickEvent);

                        // Sleep for the interval between messages
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        System.err.println("Error while producing messages: " + e.getMessage());
                    }
                }

                System.out.printf("Producer stopped after %d ms.%n", System.currentTimeMillis() - startTime);
            });
        } catch (Exception e) {
            System.err.println("Failed to start producer: " + e.getMessage());
        }
    }

    /**
     * Stops the message production.
     */
    public void stopProducer() {
        try {
            running = false; // Stop producing messages
            if (executorService != null) {
                executorService.shutdown();
                executorService.awaitTermination(5, TimeUnit.SECONDS); // Wait for threads to finish
            }
            if (producer != null) {
                producer.close(); // Close the Kafka producer
            }
            System.out.println("Message production stopped successfully.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while stopping the producer: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error while stopping the producer: " + e.getMessage());
        }
    }

    /**
     * Generates a random number between `min` and `max`.
     *
     * @param min Minimum value (inclusive).
     * @param max Maximum value (exclusive).
     * @return Random integer between min and max.
     */
    private static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }
}