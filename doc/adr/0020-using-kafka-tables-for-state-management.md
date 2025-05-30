# ADR-0020: Using Kafka Tables for State Management

## Status
Accepted

## Context
Our system needs to track the state of blocks, monitor machine operations, and maintain processing statistics. This requires reliable state management that can handle real-time updates, support queries, and recover from failures. For example, we need to know how many blocks of each color have been processed, track the current state of machines, and maintain windowed statistics for monitoring.

## Decision
We will implement Kafka Tables (KTable) for state management in our stream processing applications. We will do this through:

1. Block Processing State
   - The `BlocksHandledTable` maintains a count of processed blocks by color
   - Example: When a `BlockSorted` event arrives, we update the count for that color in the table
   - This state is used to track processing statistics

2. Windowed Statistics
   - The `WindowedBlocksHandledTable` maintains time-windowed statistics
   - Example: We track the number of blocks processed in 5-minute windows
   - This state is used for monitoring and performance analysis

3. Performance Monitoring
   - The `MonitoringStream` calculates average processing times
   - Example: We track the time between `ConveyorMoveBlock` commands and `ConveyorBlockMoved` events
   - This state is used to monitor system performance and detect bottlenecks


## Consequences
The implementation of Kafka Tables provides:

Positive:
- Reliable tracking of block processing statistics
- Real-time monitoring of system performance
- Efficient state lookups for operational decisions
- Automatic state recovery after failures
- Sets us up for implementing interactive queries in the future

Negative:
- Additional storage needed for state maintenance
- Need to implement state cleanup for old data
- Performance monitoring required for large state tables
- Careful management of state serialization/deserialization
