# 10. implementing mock server

Date: 2025-03-18

## Status

Accepted 

Superceded by [25. Building a happy path](0025-building-a-happy-path.md)

## Context

The hardware for this porject is not readily available online for us to use and needs to be accessed via the local network in the lab. This makes testing our system and devloping for it more difficult. 

## Decision

To facilitate easier trials and aid implementation a mock server was implemented to imitate the robot and convoy services. 

## Consequences

This allows us to already implement some services and make sure they are working correctly before testing it in the lab and work with the physical hardware.
