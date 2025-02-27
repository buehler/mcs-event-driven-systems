# Event Driven Architecture

This is the project implementation for the event driven and process oriented architecture
course. The project works with the provided robot simulator in the lab.

## General workflow

The project will be set up in microservices and communicates via Kafka. The general idea
of the process that is modelled is as follows:

A delivery service will deliver a package from a supplier to a warehouse. The warehouse
has an automated inventory system. An inventory person can set the package on the
predefined spot and then access the web UI to announce a delivery with the respective
delivery notice. Then, the warehouse inventory system will start the process of checking
the delivery. The happy-path includes:

- Each part is picked up by the robot in one of the 3x3 grid cells of the predefined location
- Each available part is then put onto the conveyor and driven down to the color sensor
- The second robot will pick it up and puts it onto the color sensor
- After the color has been determined, the robot puts it back to the conveyor
- After being driven back, the first robot sorts the color onto the respective stack
- If a part is marked with an NFC tag, it means that the part is defective and should
  be put into the defective stack, regardless the color

## Sequence

Please take note that the event based communication is not part of the following sequence diagram.
The diagram shows the happy path if all services are working correctly.

```mermaid
sequenceDiagram
  actor u as User
  participant i as Inventory UI
  participant o as Orchestrator
  participant p as Picker Robot
  participant belt as Conveyor
  participant c as Color Robot

  u ->> i: fill in delivery notice
  i ->> o: notify about delivery

  loop For each part
    o ->> p: get part
    p ->> belt: put onto
    belt -->> c: drive to color bot
    c ->> belt: get part
    c ->> c: check color
    c ->> belt: put back
    belt -->> p: drive back
    p ->> p: check for defects
    p ->> p: stack accordingly
    p ->> o: notfiy
  end

  o ->> i: notify
```

## Possible error scenarios

- In general: if a robot is not able to pick up a certain part
- If the delivery contains wrong information about the amount of parts
- If during the inventoring process, parts are lost
