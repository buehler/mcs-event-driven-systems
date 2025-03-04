# Exercise 2 - Suggestions for Software Project

The implementations that have already been developed have been pushed to our public repo: `https://github.com/buehler/mcs-event-driven-systems/`.

For our semester project for this course the group has decided to implement a workflow the uses the two robotic arms in the lab that are positioned on either end of a conveyor belt. The hardwear that is present in the lab that can be integrated in this workflow further include a colour sensor, accelorometer, push buttons, proximity sensory and an NFC sensor (and possibly more). The workpieces that are present are different colours cubes, some of which are outfittet with an NFC tag. Further some user tasks will be incorporated where the user will start the flow of the process. 

The rough flow of overall process will looks something like:

1. Shipment manifest is created by user, detailing how many blocks are "delivered", these blocks are placed by the user in the devided intake grid, a 9x9 grid
2. The robot arm will start a validation process by seeing if the shipment manifest is the same as the acutal delivery of the blocks in the grid
3. There will be some further processing of the block such as checking for an NFC tag
4. The robots will keep an inventory list and stack the block that have arrived in a to-be-determind logic.

The exact workflow is still very open as technical feasability is evaluated and different microservices are implementet step-by-step. The possible flows have been drawn up by us, one can be found in the README on our Github. The other is a Miro Board, the like to which can be found at the bottom of the page.





The ADRs that have been written on the architectual decisions are documented `/doc/adr` and will be updated regularly over the course of the semester.

The contributions of the different team members can be found`/doc/contributions_log` and will be updated regularly over the course of the semester.

Link to our Miro board: `https://miro.com/welcomeonboard/TmNQTEpOM3JLakVmVUx5K2tmbDE5Tmp6eVhGTmxuTHN3eTJlZUdHTm9ZbDdBZlZRUTFkMTVjcWVoeTNKMGE2TzQ5UUk2a29XVlZLcENzRmdTUytZVFJKQjVoZGh4enJvSHA3SlB0VTBKK3BjOUdkV3MwVXQ5QkQycHgySS90ZlZzVXVvMm53MW9OWFg5bkJoVXZxdFhRPT0hdjE=?share_link_id=771121353483`.
