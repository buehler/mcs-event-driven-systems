# EDPO Testing Documentation

## Overview

This document outlines the testing infrastructure for the system. The testing framework provides a way to simulate the shipment processing workflow with configurable parameters and failure points.

## Testing Endpoints

The system provides two main endpoints for testing:

1. `/mock/testing` - Initiates a test run with configurable parameters
2. `/mock/endTest` - Terminates a running test and cleans up resources

See [MockRunController.java](../src/manager/src/main/java/ch/unisg/edpo/manager/controllers/MockRunController.java)

## Test Workflow

### Starting a Test

To start a test, send a POST request to the `/mock/testing` endpoint with a JSON body containing test parameters. The system will:

1. Initialize the test environment
2. Create a new process instance in Camunda
3. Send the necessary events to simulate a shipment processing workflow

### Ending a Test

To end a test, send a POST request to the `/mock/endTest` endpoint. This will:

1. Stop the TestingStatusService
2. Retrieve the active Camunda process instance
3. Delete the process instance
4. Release all resources

This endpoint can also be used when live testing with the robots. It will delete the running Camunda process instance. The process can then be restarted by sending a new shipment file.

## Test Parameters

When starting a test with the `/mock/testing` endpoint, you can configure the following parameters:

| Parameter | Description                                                                   | Example |
|-----------|-------------------------------------------------------------------------------|---------|
| `shipment_id` | Unique identifier for the test shipment                                       | `"happy-path-test"` |
| `blocks` | Array notation of blocks with format `[isCorrect:color, ...]`                 | `"[true:blue, true:green, true:red, false:yellow, ...]"` |
| `defaultTimeout` | Primary timeout duration, used for error handling                             | `"PT20S"` (20 seconds) |
| `defaultTimeoutTwo` | Secondary timeout duration, used for nfc tag detection                        | `"PT2S"` (2 seconds) |
| `simulatedRobotTime` | Duration to simulate robot processing (in seconds)                            | `1` |
| `failingPoint` | Specific point in the workflow where a failure should be simulated (optional) | `"autoRetryGrid"` |

**Important**: While testing, defaultTimeout and defaultTimeoutTwo configured in [application.properties](../src/manager/src/main/resources/application.properties) or [application-dev.properties](../src/manager/src/main/resources/application-dev.properties) will be overridden during 
the initialization in [InitializeProcessDelegate.java](../src/manager/src/main/java/ch/unisg/edpo/manager/tasks/initialize/InitializeProcessDelegate.java)
If a failing point is set, the corresponding retry parameter will also be set to true during the test and therefore override its configuration.
### Block Configuration

The `blocks` parameter defines the shipment blocks with their position in a 3x3 grid and colors:
- Format: `[blockPositioned:color, blockPositioned:color, ...]`
- `blockPositioned`: Boolean indicating if a block is available at this position (`true` or `false`)
- `color`: Color of the block (e.g., `blue`, `green`, `red`, `yellow`)

3x3 grid definition: Starting on the top-left in the array.

## Sample Requests

### Start a Standard Test

```bash
curl -X POST http://localhost:8080/mock/testing \
  -H "Content-Type: application/json" \
  -d '{
    "shipment_id": "happy-path-test",
    "blocks": "[true:blue, true:green, true:red, false:yellow, false:red, false:green, false:blue, false:blue, false:green]",
    "defaultTimeout": "PT20S",
    "defaultTimeoutTwo": "PT6S",
    "simulatedRobotTime": 5
  }'
```

### Start a Test with Failure Point

```bash
curl -X POST http://localhost:8080/mock/testing \
  -H "Content-Type: application/json" \
  -d '{
    "shipment_id": "failing-test",
    "blocks": "[true:blue, true:green, true:red, false:yellow, false:red, false:green, false:blue, false:blue, false:green]",
    "defaultTimeout": "PT5S",
    "defaultTimeoutTwo": "PT2S",
    "simulatedRobotTime": 1,
    "failingPoint": "grid"
  }'
```

### End a Running Test

```bash
curl -X POST http://localhost:8080/mock/endTest
```

## Failure Points

The testing framework supports simulating failures at different points in the workflow:

| Failure Point | Description                                                                  |
|---------------|------------------------------------------------------------------------------|
| `grid` | Simulates a failure during block movement from grid to NFC sensor            |
| `conveyor` | Simulates a failure during block movement from NFC sensor to conveyor belt   |
| `green` | Simulates a failure during green block sorting                               |
| `color` | Simulates a failure during block movement from conveyor belt to color sensor |
| `sorting` | Simulates a failure during non-green block sorting                           |

## TestingStatusService

The `TestingStatusService` maintains the state of the test and manages the simulation of events and timeouts. It provides:

- Test state management
- Configured failure point tracking
- Timeout handling for various stages of the workflow
- Block processing simulation based on the configured block array

See [TestingStatusService.java](../src/manager/src/main/java/ch/unisg/edpo/manager/testing/TestingStatusService.java)

## Command and Event Listener roles while Testing
Both the command and event listener are responsible to emmit the necessary events to run the tests. They only do this while the test service is running.
E.g. When a MoveBlockFromShipmentToNfc is received the command listener emmits the following events:
- NFCDistDetected (mocking distance sensor)
- BlockPositionedOnNfc (mocking robot) 
- If it is a green block also NFCObjectDetected (mocking NFC reader)

See the following files for the implementation:
- [CommandListener.java](../src/manager/src/main/java/ch/unisg/edpo/manager/listeners/CommandListener.java)
- [EventListener.java](../src/manager/src/main/java/ch/unisg/edpo/manager/listeners/EventListener.java)

## Testing Sequence

A typical test follows this sequence:

1. Client sends a test request with parameters
2. System initializes the `TestingStatusService` with the provided parameters
3. A new shipment processing command is dispatched with the configured `shipment_id`
4. The system simulates sensor events based on configured timeouts (`defaultTimeout` and `defaultTimeoutTwo`)
5. Block processing is simulated according to the `blocks` parameter, respecting the correctness flags
6. Robot processing is simulated with the duration specified in `simulatedRobotTime`
7. If a failure point is specified, the system simulates that failure
8. The test continues until completion or is manually terminated via `/mock/endTest`

## Response Format

Responses from both endpoints are returned as JSON. A successful test initiation returns:

```json
{
  "status": "success",
  "message": "Test started with shipment ID: happy-path-test"
}
```

A successful test termination returns:

```json
{
  "status": "success",
  "message": "Test ended and resources cleaned up"
}
```

## Considerations

- Only one test can run at a time
- Tests will continue running until they complete or are manually terminated
- The system will log detailed information about the test progress (log entries start with "Testing:" )
- The blocks parameter allows detailed customization of the test scenario

## Troubleshooting

If you encounter issues with the testing system:

1. Ensure only one test is running at a time
2. Check the logs for detailed information about the test execution
3. Use the `/mock/endTest` endpoint to clean up any hanging test resources
4. Verify that the Camunda engine is running correctly
5. Check that the block configuration follows the correct format