package ch.unisg.edpo.manager.tasks.process;

import ch.unisg.edpo.proto.events.inventory.v1.ShipmentProcessed;
import ch.unisg.edpo.manager.producer.EventProducer;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EndProcessServiceTask implements JavaDelegate {

    private final EventProducer eventProducer;

    // Constructor injection for the EventProducer
    public EndProcessServiceTask(EventProducer eventProducer) {
        this.eventProducer = eventProducer;
    }

    @Override
    public void execute(DelegateExecution execution) {
        log.info("Executing task in class: {}", this.getClass().getSimpleName());

        try {
            // Retrieve the business key from the process instance
            String shipmentId = execution.getBusinessKey();

            if (shipmentId == null || shipmentId.isEmpty()) {
                log.error("Shipment ID is null or empty. Cannot process ShipmentProcessed event.");
                throw new IllegalStateException("Business key (shipmentId) cannot be null or empty");
            }

            log.info("Retrieved business key (shipmentId): {}", shipmentId);

            // Build the ShipmentProcessed Protobuf event
            ShipmentProcessed event = ShipmentProcessed.newBuilder()
                    .setShipmentId(shipmentId)
                    .build();

            // Send the event using the EventProducer
            eventProducer.sendEvent(event, "ShipmentProcessed");

            log.info("ShipmentProcessed event sent successfully for shipment ID: {}", shipmentId);
        } catch (Exception e) {
            log.error("Error while processing EndProcessServiceTask:", e);
            // Optionally rethrow the exception if it should propagate
            throw new RuntimeException("Error while processing EndProcessServiceTask", e);
        }

        log.info("Task execution for EndProcessServiceTask completed.");
    }
}