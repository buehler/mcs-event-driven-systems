# ADR-0018: Using Kafka Streams for Real-time Data Processing

## Status
Accepted

## Context
We require real-time data processing capabilities that traditional batch processing methods cannot provide. The need for immediate data transformation and processing necessitates a stream processing solution. Our system processes sensor data, machine events, and commands that require immediate processing and state tracking.

While Kafka topics provide the basic messaging infrastructure, they lack the capabilities needed for complex stream processing operations. Topics alone cannot:
- Perform real-time data transformations and aggregations
- Maintain state between operations
- Handle windowed operations for time-based analysis
- Provide exactly-once processing guarantees
- Manage complex processing topologies

## Decision
We will implement Kafka Streams as our primary stream processing framework. The following specific streams will be implemented:

1. Sensor Data Processing Stream
   - Processes real-time sensor data from the sensors topic
   - Filters and transforms sensor readings

2. Block Handling Stream
   - Tracks block movements and sorting operations
   - Maintains state of blocks in the system
   - Processes events from the events topic
   - Implements aggregation for block statistics

3. Monitoring Stream
   - Processes conveyor movement events
   - Calculates average processing times
   - Tracks system performance metrics
   - Provides real-time monitoring data

4. Windowed Processing Stream
   - Implements time-based aggregations
   - Processes data in 30-second windows
   - Tracks block handling statistics over time
   - Provides historical trend analysis

Kafka Streams provides a comprehensive stream processing library that integrates natively with Kafka, offering exactly-once processing semantics and robust fault tolerance mechanisms.

Key advantages of Kafka Streams over raw topic consumption:
- Stateful Processing: Maintains state between operations (e.g., counting blocks by color)
- Windowing: Enables time-based aggregations (e.g., 30-second processing windows)
- Stream Processing DSL: Provides high-level operators for complex transformations
- Interactive Queries: Enables querying the current state of processing

## Consequences
Kafka Streams implementation will enable processing with native Kafka integration. The framework's stateful processing capabilities will allow for complex stream transformations. However, this decision introduces a learning curve for the development team and requires careful consideration of state management strategies to ensure optimal performance and reliability.

