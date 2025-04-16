package com.aggregator;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class LogAggregator {

    // Settings
    private static final double timeIntervalInSeconds = 0.5;
    private static final String LOG_DIRECTORY_PATH = "./exercise1/faultToleranceTest/logs";

    public static void main(String[] args) {
        System.out.println("Log Aggregator started...");

        try {
            // Locate the newest log file dynamically
            String newestLogFile = findNewestLogFile();
            if (newestLogFile == null) {
                System.err.println("No log file found in directory: " + LOG_DIRECTORY_PATH);
                return;
            }

            // Define input file name and aggregation interval within the main function
            String inputFileName = newestLogFile; // Use the newest log file dynamically

            System.out.println("Aggregating log file: " + inputFileName);

            // Perform log aggregation
            aggregateLogFile(inputFileName, timeIntervalInSeconds);
        } catch (IOException e) {
            System.err.println("Error during log aggregation: " + e.getMessage());
        }
    }

    private static String findNewestLogFile() {
        try {
            return Files.list(Paths.get(LOG_DIRECTORY_PATH))
                    .filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".log"))
                    .max(Comparator.comparingLong(f -> f.toFile().lastModified()))
                    .map(path -> path.getFileName().toString())
                    .orElse(null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void aggregateLogFile(String inputFileName, double timeIntervalInSeconds) throws IOException {
        // Define the base path for logs
        Path logsDir = Paths.get(LOG_DIRECTORY_PATH);

        // Construct the input and output paths dynamically
        Path inputPath = logsDir.resolve(inputFileName);
        Path outputProducersPath = logsDir.resolve("aggregated/producer.log");
        Path outputConsumersPath = logsDir.resolve("aggregated/consumer.log");
        Path outputEventsPath = logsDir.resolve("aggregated/events.log");

        // Ensure that the "aggregated" subdirectory exists
        Files.createDirectories(outputProducersPath.getParent());

        // Prepare a formatter for the timestamps and maps to store aggregated counts and events
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        Map<AggregatedKey, Integer> producerAggregationMap = new TreeMap<>();
        Map<AggregatedKey, Integer> consumerAggregationMap = new TreeMap<>();
        List<String> eventList = new ArrayList<>();

        long timeIntervalInMillis = (long) (timeIntervalInSeconds * 1000); // Convert seconds to milliseconds

        // Read and process the log file line by line
        try (BufferedReader reader = Files.newBufferedReader(inputPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Split and process each line
                String[] tokens = line.split(";");
                if (tokens.length < 6) {
                    continue; // Ignore malformed lines
                }

                // timestamp;eventType;thread;value;partition;offset
                String timestampStr = tokens[0].trim();
                String eventType = tokens[1].trim();
                String eventValue = tokens[3].trim();
                String threadsStr = tokens[2].trim();
                String partition = tokens[4].trim();
                String offset = tokens[5].trim();

                if ("producer".equals(eventType) || "consumer".equals(eventType)) {
                    // Parse timestamp and round to the next fitting time interval
                    LocalDateTime timestamp = LocalDateTime.parse(timestampStr, formatter);
                    long millisSinceEpoch = timestamp.toInstant(ZoneOffset.UTC).toEpochMilli();
                    long roundedMillis = millisSinceEpoch
                            + (timeIntervalInMillis - (millisSinceEpoch % timeIntervalInMillis)) % timeIntervalInMillis;
                    LocalDateTime roundedTimestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(roundedMillis), ZoneOffset.UTC);

                    // Parse threads value and aggregate for this event type and timestamp
                    AggregatedKey key = new AggregatedKey(roundedTimestamp, eventType);
                    if ("producer".equals(eventType)) {
                        producerAggregationMap.put(key, producerAggregationMap.getOrDefault(key, 0) + 1);
                    } else if ("consumer".equals(eventType)) {
                        consumerAggregationMap.put(key, consumerAggregationMap.getOrDefault(key, 0) + 1);
                    }


                } else if ("event".equals(eventType)) {
                    // Handle all entries where eventType = "event" and add them to the event list
                    eventList.add(String.format("%s;%s", timestampStr, threadsStr));
                }
            }
        }

        // Write the aggregated producer results to the producer log file
        try (BufferedWriter producerWriter = Files.newBufferedWriter(outputProducersPath)) {
            producerWriter.write("timestamp;eventType;value");
            producerWriter.newLine();
            for (Map.Entry<AggregatedKey, Integer> entry : producerAggregationMap.entrySet()) {
                AggregatedKey key = entry.getKey();
                producerWriter.write(String.format("%s;%s;%d", key.timestamp.format(formatter), key.eventType, entry.getValue()));
                producerWriter.newLine();
            }
        }

        // Write the aggregated consumer results to the consumer log file
        try (BufferedWriter consumerWriter = Files.newBufferedWriter(outputConsumersPath)) {
            consumerWriter.write("timestamp;eventType;value");
            consumerWriter.newLine();
            for (Map.Entry<AggregatedKey, Integer> entry : consumerAggregationMap.entrySet()) {
                AggregatedKey key = entry.getKey();
                consumerWriter.write(String.format("%s;%s;%d", key.timestamp.format(formatter), key.eventType, entry.getValue()));
                consumerWriter.newLine();
            }
        }

        // Write the events to the events log file
        try (BufferedWriter eventsWriter = Files.newBufferedWriter(outputEventsPath)) {
            eventsWriter.write("timestamp;event");
            eventsWriter.newLine();
            for (String event : eventList) {
                eventsWriter.write(event);
                eventsWriter.newLine();
            }
        }

        System.out.println("Aggregation complete.");
        System.out.println("Producers log saved to: " + outputProducersPath);
        System.out.println("Consumers log saved to: " + outputConsumersPath);
        System.out.println("Events log saved to: " + outputEventsPath);
    }

    // Helper class for map keys
    private static class AggregatedKey implements Comparable<AggregatedKey> {
        private final LocalDateTime timestamp;
        private final String eventType;

        public AggregatedKey(LocalDateTime timestamp, String eventType) {
            this.timestamp = timestamp;
            this.eventType = eventType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(timestamp, eventType);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            AggregatedKey other = (AggregatedKey) obj;
            return Objects.equals(timestamp, other.timestamp) && Objects.equals(eventType, other.eventType);
        }

        @Override
        public int compareTo(AggregatedKey other) {
            int timestampCompare = this.timestamp.compareTo(other.timestamp);
            return (timestampCompare != 0) ? timestampCompare : this.eventType.compareTo(other.eventType);
        }
    }
}