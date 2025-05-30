# ADR-0019: Implementing Stateless Processing in Kafka Streams

## Status
Accepted

## Context
Our event-driven system processes various types of events including sensor data, block movements, and machine commands. While some operations require state maintenance (like block counting and windowed aggregations), many operations can be optimized by implementing stateless processing patterns. This is particularly relevant for our sensor data processing and event transformation pipelines where historical context is not necessary.

## Decision
We will implement stateless processing patterns for specific operations in our Kafka Streams applications:

1. Sensor Data Processing
   - Filtering sensor events based on allowed combinations (as seen in SensorsStream)
   - Direct transformation of sensor readings to business events
   - Example: In our SensorsStream, we process sensor readings by first filtering out null values, then checking if the sensor's address is in our allowed combinations list, and finally transforming the readings into business events. This is stateless because each reading is processed independently without needing to remember previous readings.

2. Event Transformation
   - Simple mapping operations between different event types
   - Example: Our MonitoringStream demonstrates this by filtering incoming commands to only process specific types (like ConveyorMoveBlock commands) and transforming their keys to monitoring-specific keys (like "avg-conveyor-moving"). Each command is processed in isolation, making this a stateless operation.

3. Command Processing
   - Filtering events for sorting
   - Example: In our BlocksHandledTable, we process block sorting events by filtering for BlockSorted events and grouping them by color. This grouping operation is stateless as it organizes events by color without maintaining any historical state.

The implementation will use Kafka Streams' stateless operators (map, filter, branch). On this stateful processes will be built.

## Consequences
The implementation of stateless processing will result in:

Positive:
- Improved throughput for sensor data processing and event transformation
- Reduced resource utilization for operations that don't require state
- Simplified deployment and scaling of stateless processing components
- Better separation of concerns between stateful and stateless operations

Negative:
- Need to carefully identify which operations can be stateless vs stateful
- Potential need to redesign some processing logic to fit the stateless paradigm
- May require additional coordination with stateful operations when both are needed

The system will maintain its existing stateful operations for block counting, windowed aggregations, and monitoring while leveraging stateless processing for appropriate use cases. 