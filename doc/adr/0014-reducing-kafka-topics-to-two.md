# 14. Reducing Kafka topics to two

Date: 2025-04-16

## Status

Supercedes [9. Three Kafka topics](0009-three-kafka-topics.md)

Superceded by [25. adding sensor kafka topic](0025-adding-sensor-kafka-topic.md)

## Context

As previously discussed we have three Kafka topics. This was a good starting point, but as the project has progressed we have found that it is not necessary to have three topics. The sensor topic was sending many events that were just a translation of the actual sensor data. This was not necessary and added complexity to the architecture. The events topic was also sending many events that were not necessary. 

## Decision

We have decided to reduce the number of topics to two: commands and events. The sensor service therefore was editted so it would only send business level events that were a result of a change in the sensor data. For example, instead of constantly sending the ID of an NFC tag that was registered by the NFC sensor, now we simply send one higher level event when a NFC tag is detected and a further update once it is removed

## Consequences

The consequence of this, is that we only have "useful" events in the events topic. This reduces the complexity of the architecture and makes it easier to understand. The sensor service is now also less complex, as it only sends business level events. This does however have the trade-off that the sensor service is now more tightly coupled to the business logic. This is not a problem for this project, but it means that if more events are 
needed from a sensor the sensor service will need to be changed as well as the services that want to receive this new business level event. 
