package com.testEvaluation;

import java.time.LocalDateTime;
import java.util.*;

public class LogData {
    // Separate lists for producers and consumers
    private final List<LogMessage> producers = new ArrayList<>();
    private final List<LogMessage> consumers = new ArrayList<>();

    // List of events (used to mark start and end)
    private final List<Event> events = new ArrayList<>();

    /**
     * Add a log message record to the producers or consumers list.
     *
     * @param timestamp The timestamp of the event.
     * @param eventType Either "producer" or "consumer".
     * @param threadNumber The thread that generated the message.
     * @param partition The message partition (not directly used here).
     */
    public void addMessage(LocalDateTime timestamp, String eventType, String threadNumber, String partition) {
        LogMessage message = new LogMessage(timestamp);

        if ("producer".equals(eventType)) {
            producers.add(message);
        } else if ("consumer".equals(eventType)) {
            consumers.add(message);
        }
    }

    /**
     * Add an event with a name (like 'start' or 'end').
     *
     * @param timestamp Timestamp of the event.
     * @param eventName Name of the event (e.g., "start" or "end").
     */
    public void addEvent(LocalDateTime timestamp, String eventName) {
        events.add(new Event(timestamp, eventName));
    }

    /**
     * @return The list of producer messages.
     */
    public List<LogMessage> getProducers() {
        return producers;
    }

    /**
     * @return The list of consumer messages.
     */
    public List<LogMessage> getConsumers() {
        return consumers;
    }

    /**
     * @return The timestamp of the start event, or null if not found.
     */
    public LocalDateTime getStartTime() {
        return events.stream()
                .filter(event -> event.name.equals("start"))
                .map(event -> event.timestamp)
                .findFirst()
                .orElse(null);
    }

    /**
     * @return The timestamp of the end event, or null if not found.
     */
    public LocalDateTime getEndTime() {
        return events.stream()
                .filter(event -> event.name.equals("end"))
                .map(event -> event.timestamp)
                .findFirst()
                .orElse(null);
    }
}

// Supporting class for Log Messages
class LogMessage {
    final LocalDateTime timestamp;

    LogMessage(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

// Supporting class for Events
class Event {
    final LocalDateTime timestamp;
    final String name;

    Event(LocalDateTime timestamp, String name) {
        this.timestamp = timestamp;
        this.name = name;
    }
}