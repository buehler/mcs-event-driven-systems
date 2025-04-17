# 11. orchestrated workflow with single camunda woorkflow

Date: 2025-03-18

## Status

Accepted 

Superceded by [16. Orchestrated Workflow with single Camunda](0016-orchestrated-workflow-with-single-camunda.md)

## Context

As we have seen in the lecture, multiple instances of Camunda may be run depending on the set up and workflow of the application. In the example from the lecture, the workflows for payment and order fullfilment were seperated out into seperate instances in Camunda. This allows for further decoupling. For our project we need to decide if multiple workflows make sense and if they do how they would be split and implemented. 

## Decision

Only a single workflow of camunda will be run and implemented.The reason for this is the tightly integrated hardware that executes a workflow in a specific order. Only one workflow can be run at a single time with a single workpiece. There is no opportunity to one workpiece to "work around" or "overtake" another workpiece. As the physical execution in sequential and coupled it makes sense to reflect this in the Camunda workflow too. 

## Consequences

Only a single BPMN will be created and deployed that depicts the whole workflow for this application.
