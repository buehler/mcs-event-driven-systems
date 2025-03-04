# Inventory

This is the inventory UI application of the warehouse system.
It is written in NextJS with TailwindCSS and TypeScript.

The main goal of the application is to provide an entrypoint
into the warehouse system and allow users to interact with
the system without the need for a terminal.

Any user may open the inventory system and check the currently
available stock. They may also add new items to the inventory
by announcing a new delivery. A new delivery will trigger
the process of the robots checking the delivery and
adding them to the correct stacks.

## Storage

The current version of the inventory system runs an in-memory storage
of the items. This means that the items are not persisted between
restarts of the application.
