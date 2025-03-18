# 8. messageType in Protobuf header

Date: 2025-03-17

## Status

Accepted

## Context

We need a structure to discern between different events/commands/sensordata so that services know what they are listening to and complete the tasks only meant for the respective service.

## Decision

To ensure that services can liste to the correct commands for them we implemeted a messageType in the header of the protobuf messages. 

## Consequences

We have a system that allows us to more accuratly listen and emit events and commads to ensure that the correct messages are received by the services.
