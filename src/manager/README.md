# Manager

This is the orchestrator of the overall software.
The orchestrator is required to oversee the whole process
from beginning to the end and to react if errors occur.

## Settings
Via the settings
- [application.properties](src/main/resources/application.properties)
- [application-dev.properties](src/main/resources/application-dev.properties)

### Timeout
- robot.timeout.duration
  - Timeout, how long camunda waits for events to arrive
- robot.timeout.duration2
  - Second timeout time for testing purposes
### Retry mechanism
The retry mechanism can be switched on and off per critical path:
- Retry to grab the block from the initial grid 
  - robot.auto.retry.grid
- Retry to move block on the conveyor belt
  - robot.auto.retry.conveyor
- Retry to grab the block from the NFC reader 
  - robot.auto.retry.nfc
- Retry to grab the block from the conveyor in front of left sensor 
  - robot.auto.retry.color
- Retry to grab the block from the color sensor
  - robot.auto.retry.sorting

Camunda will then do an automatic retry for this actions before creating a manual user task.
