# 9. Three Kafka topics

Date: 2025-03-18

## Status

Accepted 

Superceded by [14. Reducing Kafka topics to two](0014-reducing-kafka-topics-to-two.md)

## Context

To move forward with the implementation, how and what topics for Kafka will be used, need to be decided. 

## Decision

To strike a balance between order and chaos we decided on implementing and working with three topics for Kafka. These three topics are:

Commands - here all commands are sent for execution which the services listen too. Typically these are 1:1

Events - here events are posted by different services that may have multiple listeners

Sensor - there the sensors that may emit a lot of messages post their sensor data, this was deliberatly excluded from the events topic so it does not get spammed 

## Consequences

Workig with these three topics in Kafka for the purpose of this project. For every service that is implemented it has to be decided which of these three topics the services listens too and where it emits messages.
