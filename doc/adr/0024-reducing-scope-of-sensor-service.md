# 24. Reducing Scope of Sensor Service

Date: 2025-05-30

## Status

Accepted

Supercedes [17. Implementing business logic into sensor service](0017-implementing-business-logic-into-sensor-service.md)

## Context

In our previous architecture, as described in ADR-0017, the sensor service was responsible for translating raw sensor data from MQTT into business-level events before forwarding them to Kafka. While this approach simplified downstream processing, it tightly coupled business logic to the sensor service and limited our ability to leverage Kafka's advanced stream processing features. As our system evolved, we identified the need for more flexible event processing, including stateless transformations, aggregations, and windowed operations, which are better handled in a dedicated streams service.

## Decision

We decided to reduce the scope of the sensor service so that it now performs a simple 1:1 translation of incoming MQTT sensor data into Kafka events, without embedding any business logic or event aggregation. All business-level event processing and transformations are now handled in the streams service, see [26. Adding Streams Service](0026-adding-streams-service.md).


## Consequences

This change decouples business logic from the sensor service, making the architecture more modular and maintainable. It enables us to implement advanced Kafka stream processing, such as stateless operations and aggregations, in a dedicated service. The sensor service is now simpler and easier to test, while the streams service can be extended to support more complex processing requirements. One potential downside is that the streams service now assumes greater responsibility for event processing, which may increase its complexity. However, this trade-off is justified by the increased flexibility and scalability of the overall system.
