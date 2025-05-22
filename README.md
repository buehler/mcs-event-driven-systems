# Event Driven Architecture

This repository contains the implementation for the event-driven and process-oriented architecture course. The project works with the provided robot simulator in the lab and is structured as a set of microservices communicating via Kafka.

## Project Overview & Workflow

The system models a delivery and inventory process in a warehouse with automated robots. The general workflow is as follows:

1. A delivery service brings a package to the warehouse.
2. An inventory manager places the package in a predefined spot and announces the delivery via the web UI.
3. The warehouse inventory system checks the delivery:
   - Robots pick up parts from a 3x3 grid, place them on an NFC reader.
   - Parts are moved onto a conveyor belt, pass a color sensor, and are sorted by robots.

Error scenarios (e.g., failed pickups, incorrect delivery info, lost parts) are handled with manual intervention after a 5-second timeout.

## Getting Started

You can run the project either using Docker (recommended for most users) or manually (for development/debugging purposes).

### 1. Running with Docker (Recommended)

Docker will handle building, protobuf generation, and running all services for you.

- **Build and start all services:**

  ```sh
  just compose-dev build
  just compose-dev up -d
  ```

  - `compose-dev build` will build all services and generate protobuf files automatically.
  - `compose-dev up -d` will start the entire application stack in the background.

- **Service Endpoints:**

  - Camunda: [http://localhost:8080](http://localhost:8080) (credentials: `a/a`)
  - Frontend: [http://localhost:3000](http://localhost:3000)
  - Kafka broker: `localhost:9092`

- **Listing available commands:**
  ```sh
  just
  ```

### 2. Manual Development/Debugging (Without Docker)

If you want to run or debug services manually (e.g., in your IDE):

1. **Generate protobuf files:**

   - This step is required before running any services manually.
   - Run:
     ```sh
     buf generate
     ```

2. **Start services in development mode:**

   - Start each service as needed, ensuring the `dev` Spring Boot profile is active for the Manager application.

3. **Service Endpoints:**
   - Camunda: [http://localhost:8080](http://localhost:8080) (credentials: `a/a`)
   - Frontend: [http://localhost:3000](http://localhost:3000)
   - Kafka broker: `localhost:9092`

## Using the `just` Command

[`just`](https://github.com/casey/just) is a command runner used to manage project-specific commands defined in the `justfile` at the root of this repository.

### Installation

On macOS, install with Homebrew:

```sh
brew install just
```

Or see the [just GitHub releases page](https://github.com/casey/just/releases) for other platforms.

### Usage

- List all available recipes:
  ```sh
  just --list
  ```
- Run a specific recipe:
  ```sh
  just <recipe-name>
  ```
- Example (simulate NFC sensor):
  ```sh
  just sensor-nfc-value yes
  ```

## Syntax for Just Commands

- NFC sensor: `just sensor-nfc-value yes` or `just sensor-nfc-value no` (the sensor remembers its last state, so use `no` before firing another `yes`)

## Error Handling

- If a robot cannot pick up a part, if delivery info is wrong, or if parts are lost, the system waits 5 seconds for events. If unresolved, manual intervention is triggered and the last step is repeated.

## Further Information

For more details on the process model, error scenarios, and available commands, see the `justfile` and the documentation in the `doc/` directory.

## Testing: Happy Path and Error Path

Automated testing of the process, including both the happy path and error scenarios, is supported via dedicated HTTP endpoints implemented in the `MockRunController` (see `manager/src/main/java/ch/unisg/edpo/manager/controllers/MockRunController.java`).

### General Idea

- The testing endpoints allow you to simulate a full process run (happy path) or inject errors at specific points (error path) without manual interaction with the UI or hardware.
- The controller manages the test state, simulates events, and can trigger failures at configurable steps in the process.

### How to Start a Test

1. **Start a test by sending a POST request to `/mock/testing`**

   - The request body must include:

     - `shipment_id`: Unique identifier for the shipment.
     - `blocks`: List of blocks and their colors, e.g., `[true:green, false:red, true:blue]` (see the controller for the exact format).
     - `defaultTimeout` and `defaultTimeoutTwo`: ISO-8601 durations (e.g., `"PT5S"` for 5 seconds) to configure timeouts.
     - `simulatedRobotTime`: Integer (milliseconds) to simulate robot action duration.
     - `failingPoint`: (Optional) String to inject an error at a specific process step (`grid`, `conveyor`, `green`, `color`, `sorting`, or `none`).

   - Example request (using `curl`):
     ```bash
     curl -X POST http://localhost:8080/mock/testing \
       -H "Content-Type: application/json" \
       -d '{
         "shipment_id": "happy-path-test",
         "blocks": "[true:blue, true:green, true:red, false:yellow, false:red, false:green, false:blue,  false:blue, false:green]",
         "defaultTimeout": "PT20S",
         "defaultTimeoutTwo": "PT6S",
         "simulatedRobotTime": 5
       }'
     ```
   - The test will start and simulate the process according to the provided configuration.

2. **End a test by sending a POST request to `/mock/endTest`**
   - This will stop the test, clean up resources, and terminate any active process instance.
   - Example:
     ```sh
     curl -X POST http://localhost:8080/mock/endTest
     ```

### Configuration Options

- **Happy Path:** Set `failingPoint` to `none` to run the process without injected errors.
- **Error Path:** Set `failingPoint` to one of the supported values (`grid`, `conveyor`, `green`, `color`, `sorting`) to simulate a failure at that step. The system will trigger manual intervention logic as described in the process model.
- **Timeouts and Robot Time:** Adjust `defaultTimeout`, `defaultTimeoutTwo`, and `simulatedRobotTime` to control the timing of events and error handling.

For more details, see the implementation in `MockRunController.java` and
the [Testing Documentation](doc/Testing Documentation.md)
