# 4. Using protobuf for messages

Date: 2025-03-04

## Status

Accepted

## Context

To make sure we have good compability and are congruent with the messages that are sent in kafka a decision needed to be made to which standard would be used throughout this project. 

## Decision

The decision was made to use protobuf due to its compact and efficent way of serializing structured data. 

## Consequences

Protobuf will be used for all communication between our services.
