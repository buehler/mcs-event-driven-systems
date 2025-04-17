# 15. Aggregator Pattern

Date: 2025-04-17

## Status

Accepted

## Context

In our event-driven architecture, certain stages of a business process depend on receiving multiple asynchronous events from different services. These events may arrive at unpredictable times, and the process should only proceed when all required messages have been received.

We also needed robust handling for missing or delayed events, to prevent workflows from stalling indefinitely and to enable appropriate fallback or error-handling measures.

## Decision

We implemented the Aggregator Pattern using BPMN in Camunda, making use of the following modeling constructs:

- A collapsed sub-process encapsulates the aggregation logic and serves as a dedicated scope for message collection.
- Inside the sub-process, we defined multiple message intermediate catch events, each waiting for a specific expected event.
- These catch events run in parallel, allowing the process to wait asynchronously for multiple events.
- A boundary timer event is attached to the sub-process to enforce a timeout. If not all messages arrive within the configured time window, the timer event triggers an alternative path for error handling or compensation.

## Consequences

Benefits:
- Provides a clear, declarative way to model event aggregation in BPMN.
- Waits for all required events in parallel, maximizing responsiveness.
- Timeout behavior is cleanly managed through the boundary timer event.
- Aggregation logic is reusable and isolated via the sub-process.

Trade-offs:
- The number of parallel message catch events increases process complexity.
- Correct correlation setup is critical to avoid lost or mismatched events.
- Timeout configuration must balance between responsiveness and tolerance for delays.