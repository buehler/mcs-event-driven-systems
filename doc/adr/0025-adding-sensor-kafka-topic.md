# 25. Adding Sensor Kafka Topic

Date: 2025-05-30

## Status

Accepted

Supercedes [14. Reducing Kafka topics to two](0014-reducing-kafka-topics-to-two.md)

## Context

Following the decision in ADR-0024 to reduce the scope of the sensor service to a simple translator, we identified the need for a dedicated Kafka topic to handle raw sensor data. Previously, our architecture used only two Kafka topics: a command-bus and an event-bus, as described in ADR-0014. However, with the sensor service now forwarding all MQTT sensor messages directly to Kafka without business logic, a separate topic is required to avoid mixing raw sensor data with business events and commands. This separation ensures clarity and maintainability in our event-driven architecture.

## Decision

We decided to introduce a new Kafka topic named "sensor" specifically for raw sensor data. The sensor service publishes all incoming MQTT messages to this topic without modification. Downstream services, such as the streams service, consume from the sensor topic to perform business-level event processing and aggregations. The command-bus and event-bus topics remain in use for their respective purposes.

## Consequences

Adding a dedicated sensor topic improves the modularity and clarity of our Kafka architecture. It allows us to cleanly separate raw sensor data from business events and commands, making it easier to manage and process each type of message appropriately. This change also supports the implementation of advanced stream processing in downstream services. One consideration is the slight increase in the number of Kafka topics, but this is outweighed by the benefits of improved maintainability and scalability. The previous approach of using only two topics is now superseded by this more flexible and robust design.
