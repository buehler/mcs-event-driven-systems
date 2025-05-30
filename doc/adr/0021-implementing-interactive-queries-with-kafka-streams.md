# ADR-0021: Implementing Interactive Queries with Kafka Streams

## Status
Accepted

## Context
Our system needs to provide real-time access to processing statistics and monitoring data. This includes block processing counts, windowed statistics, and conveyor performance metrics. These metrics need to be accessible through a web interface for monitoring and analysis.

## Decision
We will implement interactive queries using Kafka Streams' queryable state stores. We will do this through:

1. Monitoring Controller
   - The `MonitoringController` exposes state through a web interface
   - Example: Queries the `windowed-blocks-handled-table` to show block processing statistics by color
   - Provides real-time access to processing counts in time windows

2. Performance Monitoring
   - Queries the `avg-conveyor-moving-table` to show conveyor performance
   - Example: Displays average time between commands and events
   - Shows both the average time and number of blocks processed

3. Web Interface
   - Implements a monitoring dashboard using Thymeleaf templates
   - Example: The `monitor.html` template displays:
     - Block processing statistics by color
     - Time window information
     - Conveyor performance metrics


## Consequences
The implementation of interactive queries provides:

Positive:
- Real-time access to processing statistics through a web interface
- Ability to monitor system performance and detect issues
- Consistent view of state across the application

Negative:
- Additional complexity in managing state store access
- Need to handle query routing in a distributed environment, hence why we build the web-interface in the same microservice
