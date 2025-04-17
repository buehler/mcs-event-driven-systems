# 17. Implementing business logic into sensor service

Date: 2025-04-17

## Status

Accepted

## Context

The data from the sensors is sent by MQTT. These sensor data needs to be translated to kafka events. Therefore, we need to implement a service that does this. The scope of this service will be discussed below. 

## Decision

The sensor service will be responsible for translating the sensor data to business level events. This means that the sensor service will be tightly coupled to the business logic. The sensor service will only send events that are a result of a change in the sensor data. For example, instead of constantly sending the ID of an NFC tag that was registered by the NFC sensor, now we simply send one higher level event when a NFC tag is detected and a further update once it is removed.

## Consequences

The sensor service will take the sensor data that comes via MQTT and translate this to business level events. Thi means that the other services, such as manager/camunda have an easier time interpreting them, as Camunda may struggle with getting a distance sensor reading every couple of seconds. This effectively means that there is already a some simple event processing within the sensor service. To allow for good performance this service will be written in Go.  