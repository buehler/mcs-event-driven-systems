# 7. using camunda 7

Date: 2025-03-17

## Status

Accepted

## Context

To move forward without BPMN model, and its implementation we needed to decide which version of Camunda to use. The options were Camunda7, which runs locally, but for which support is running out, or Camunda8, its new version that is cloud-based. One of the upsides of Camunda8 are the wide array of adapters that are already supplied, therefore reducing the need for self-written java adapters. One upside of Camunda7 is its local apporach which may make integrating hardware easier. 

## Decision

Due to the local Camunda7 being able to be deployed locally, we decided to go with Camunda7. It will make the process of integrating the java services a little bit more manual, however it will save us from finding some new implementation of our Kafka servers for the robots in the lab to pick up commands. This is as we need to connect the locally running hardwear in the lab with

## Consequences

Our BPMB will be created and deployed using Camunda7. It will result in us needing to do some extra implementations to call the services, while making it easier when sending commands to the robots.
One of the biggest drawback of not using Camunda8 is that Camunda7 does, in comparison, not have an implemented logic for message queueing. Th