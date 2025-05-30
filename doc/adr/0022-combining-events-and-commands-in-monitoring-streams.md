# ADR-0022: Combining Events and Commands in Monitoring Streams

## Status
Accepted

## Context
Our system needs to measure the time between commands and their corresponding events to monitor system performance. For example, we need to track how long it takes from sending a `ConveyorMoveBlock` command until the `ConveyorBlockMoved` event is received. This requires combining and correlating events from different streams.

## Decision
We will implement monitoring streams that combine commands and events to measure processing times. We will do this through:

1. Command and Event Streams
   - Filter commands and events from their respective topics
   - Example: Filter for `ConveyorMoveBlock` commands and `ConveyorBlockMoved` events
   - Use the same key ("avg-conveyor-moving") for both streams to enable correlation

2. Stream Merging
   - Merge the command and event streams into a single stream
   - Process the merged stream to calculate time differences
   - Example: When a command arrives, store its timestamp; when the corresponding event arrives, calculate the time difference

3. Time Measurement
   - Use the `AvgTimeProcessor` to track command-to-event timing
   - Maintain running averages of processing times
   - Example: Calculate average time between `ConveyorMoveBlock` and `ConveyorBlockMoved`

## Consequences
The implementation of combined monitoring streams provides:

Positive:
- Accurate measurement of system processing times
- Real-time performance monitoring
- Ability to detect processing delays
- Simple correlation of commands and events

Negative:
- Need to handle out-of-order events
- Complexity in maintaining accurate averages
- Need to handle missing or delayed events

