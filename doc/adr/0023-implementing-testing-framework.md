# ADR-0023: Implementing Testing Framework

## Status
Accepted

Supercedes [10. implementing mock server](0010-implementing-mock-server.md)

## Context
Our system needs a reliable way to test the shipment processing workflow, including both happy paths and failure scenarios. We need to simulate sensor events, robot movements, and various failure points to ensure the system behaves correctly under different conditions.

## Decision
We will implement a comprehensive testing framework that allows simulation of the entire shipment processing workflow. We will do this through:

1. Test Configuration
   - REST endpoints for test control (`/mock/testing` and `/mock/endTest`)
   - Configurable parameters for shipment blocks, timeouts, and failure points
   - Support for simulating robot processing times

2. Event Simulation
   - Command and Event Listeners that emit necessary events during testing
   - Example: When `MoveBlockFromShipmentToNfc` is received, listeners emit:
     - `NFCDistDetected` (mocking distance sensor)
     - `BlockPositionedOnNfc` (mocking robot)
     - `NFCObjectDetected` for green blocks (mocking NFC reader)

3. Failure Simulation
   - Support for simulating failures at specific points:
     - Grid movement failures
     - Conveyor belt failures
     - Color sensor failures
     - Sorting failures
   - Automatic retry mechanism for failed operations

For detailed implementation and usage instructions, see [Testing Documentation](../Testing Documentation.md).

## Consequences
The implementation of the testing framework provides:

Positive:
- Ability to test complete workflow scenarios
- Configurable failure points for error handling testing
- Realistic simulation of sensor and robot behavior
- Easy integration with existing Camunda processes

Negative:
- Need to maintain synchronization between test and real implementations
- Complexity in managing test state and cleanup
- Potential impact on system performance during testing
- Need to keep test documentation up to date

The system will use this testing framework to validate both happy paths and error scenarios, ensuring reliable operation of the shipment processing workflow. 