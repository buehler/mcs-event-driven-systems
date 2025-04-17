# 6. seperating out services from robots

Date: 2025-03-04

## Status

Accepted

## Context

In the current implementation some features, such as the conveyor belt is attached to one of the robots (and so is the colour sensor). This means, at this time, these services are tightly coupled to this robot. This may not be desirable in an event driven architecture. 

## Decision

Pending technical feasibility all of these services are to separated out, into separate microservices so that these services are decoupled and processes may happen in parallel. 

## Consequences

Technical feasibility will need to be checked. Especially the independence of the API. Questions that need to be revisited before fully implementing separate service: Can the robot and the conveyor be called at the same time or does the robots current task need to be complete before calling to the conveyor belt.
On an architectural level the consequence for this is more services that allow for separation of concern. This is also important as especially during error scenarios having the conveyor belt separate may allow the system to clear the workspace/conveyor so that other processes can continue to complete different task without the whole system coming to a standstill.
