# 26. Adding Streams Service

Date: 2025-05-30

## Status

Accepted

## Context

As our system evolved to leverage more advanced Kafka features, we identified the need for a dedicated service to manage all Kafka Streams operations and stateful processing. Previously, Kafka stream processing and table management were distributed across multiple services, which made maintenance and scalability challenging. Additionally, access restrictions and architectural considerations required us to introduce a new frontend for visualizing Kafka tables and stream processing results. With the sensor service now forwarding raw sensor data to a dedicated Kafka topic, there was also a need for a central place to process these streams into business-level events and perform aggregations.

## Decision

We decided to introduce a new service, called the streams service, responsible for all Kafka Streams processing and table management. This service consumes raw sensor data from the sensor Kafka topic, processes it into business-level events, and performs necessary aggregations to populate Kafka tables. The streams service also hosts a new frontend that provides access to real-time views of the Kafka tables and stream processing results, addressing access restrictions and improving system observability. By centralizing Kafka stream processing in a single service, we improve maintainability, scalability, and the ability to extend the system with new stream processing features.

## Consequences

The addition of the streams service brings greater modularity and clarity to our architecture. It centralizes all Kafka Streams and table operations, making the system easier to maintain and extend. The new frontend enhances accessibility and observability for users and developers. Processing sensor data into business-level events within the streams service enables more flexible and powerful event-driven workflows. One consideration is the increased responsibility and complexity within the streams service, but this is offset by the benefits of a more organized and scalable architecture. 