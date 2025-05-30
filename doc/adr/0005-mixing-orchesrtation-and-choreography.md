# 5. mixing orchesrtation and choreography

Date: 2025-03-04

## Status

Accepted

## Context

In the context of event-driven architectures two different approaches are discussed. These are orchestration and choreography. While orchestration can simplify the overall flow by providing a single point of control, it also introduces a single point of failure and potential bottlenecks. A choreography can provide greater flexibility and scalability by allowing services to react independently to events, but the lack of a central coordinator can make the system more complex to manage and troubleshoot.

## Decision

To evaluate and become familiar with the up- and down-sides of both of these flows, both will be present in different aspects of the project. The system will primarily use orchestration through the manager service (which runs Camunda) for the main workflow, while implementing choreography for specific use cases where it makes more sense.

### Orchestration Implementation
Most of the system is designed in an orchestrated way due to the inherent semantic coupling that the hardware setup has. This means that almost all commands are sent from the orchestrator to the services, and services only act when spoken to by the orchestrator. The manager service with the running deployment of Camunda serves as the central orchestrator.

### Choreography Implementation
We implemented choreography in two specific use cases:

1. **Conveyor Belt Speed Control**
   - Triggered by a rotary dial in the hardware setup
   - When the dial is turned, the sensor service emits an event into the Kafka events topic
   - The conveyor service directly consumes this event and adjusts its speed
   - Due to hardware limitations, the speed change only affects the next instruction

2. **Inventory Updates**
   - When a block is sorted, the color robot publishes a `BlockSorted` event
   - The inventory service directly consumes this event
   - Updates the GUI and database with the new block count
   - No orchestration needed for this real-time update

## Consequences

The mixed approach of orchestration and choreography provides several key benefits and considerations:

1. **Main Workflow Control**: The centralized control through Camunda provides clear process visibility and simplifies error handling, making the main workflow more manageable.

2. **Real-time Updates**: Choreography enables immediate UI and hardware updates, reducing latency for time-sensitive operations while keeping them decoupled from the main workflow.

3. **System Flexibility**: The ability to use different patterns where appropriate allows us to work around hardware limitations and maintain independent UI updates.

4. **Implementation Complexity**: The system requires developers to understand both patterns and maintain clear documentation about where each pattern is used.

The specific implementation details and examples of both patterns in use can be found in the main documentation.
