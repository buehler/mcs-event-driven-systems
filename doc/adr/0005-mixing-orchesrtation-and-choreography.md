# 5. mixing orchesrtation and choreography

Date: 2025-03-04

## Status

Accepted

## Context

In the context of event-driven architectures two different approaches are discusssed. These are orchestration and choreography. While orchestration can simplify the overall flow by providing a single point of control, it also introduces a single point of failure and potential bottlenecks.A choreography can provide greater flexibility and scalability by allowing services to react independently to events, but the lack of a central coordinator can make the system more complex to manage and troubleshoot.

## Decision

To evaluate and become familiar with the up- and down-sides of both of these flows, both will be present in different aspects of the project. 

## Consequences

Determin the process that will be replicated and decide which steps will orchestrated and which will be implemented in a choreography. The goal here is to use both in scenarios where it makes sense. As the flow is not fully determined at this stage, the decision on which exact services will use which flow will be documented later on.
