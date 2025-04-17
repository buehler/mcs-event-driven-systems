# 16. Orchestrated Workflow with single Camunda

Date: 2025-04-17

## Status

Accepted

Supercedes [11. orchestrated workflow with single camunda woorkflow](0011-orchestrated-workflow-with-single-camunda-woorkflow.md)


## Context

Camunda can be set up in various configurations depending on scalability, fault tolerance, and business needs. Typical options include:

- Running a single or multiple Camunda engine instances
- Deploying one or multiple BPMN workflows
- Allowing single or multiple concurrent process instances

However, the physical setup of our environment imposes strict limitations:

- There is only one unloading area, meaning that only one shipment can be processed at a time.
- The robots involved are tightly coupled and cannot be fully parallelized due to hardware and workflow constraints.


## Decision

- A single Camunda engine instance will run, embedded in the manager service.
- Only one BPMN workflow will be created and deployed to represent the entire shipment handling process.
- At any time, only one process instance of this workflow is allowed to be active.

Before starting a new process instance (triggered by `NewShipmentEvent`), the system checks if another instance is already running. If one is in progress, the new event is ignored.

## Consequences

Benefits:
- Matches the physical constraints of the system: only one unloading area and shared robot resources.
- Keeps the architecture simple, with minimal orchestration overhead.
- Easier to reason about and monitor, since only one process instance can be active at a time.

Drawbacks:
- Events are dropped if received while a process is running. There is no queuing or retry logic.
  - The assumption is, that the worker who places the pallet of blocks in place has to scan the shipment documents thus creating the `NewShipmentEvent`. If he does this twice, it will be the same data anyway.
- Parallel work of the left and right robot has to be addressed in the single BPMN workflow.