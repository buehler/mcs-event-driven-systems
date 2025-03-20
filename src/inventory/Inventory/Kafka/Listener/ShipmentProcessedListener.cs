using Confluent.Kafka;

using Inventory.Database;
using Inventory.Proto.Commands.Inventory.V1;
using Inventory.Proto.Events.Inventory.V1;
using Inventory.Proto.Events.Machines.V1;

using BlockColor = Inventory.Proto.Models.V1.BlockColor;

namespace Inventory.Kafka.Listener;

public class ShipmentProcessedListener(
    KafkaEventsListener events,
    ILogger<ShipmentProcessedListener> logger,
    NotificationStore notifications)
    : IHostedService
{
    private Task Handle(ShipmentProcessed message)
    {
        logger.LogInformation($"Received shipment processed message for id {message.ShipmentId}");
        notifications.Add(new("Shipment Processed", $"Shipment {message.ShipmentId} has been processed"));

        return Task.CompletedTask;
    }

    public Task StartAsync(CancellationToken cancellationToken)
    {
        events.AddHandler<ShipmentProcessed>(Handle);
        return Task.CompletedTask;
    }

    public Task StopAsync(CancellationToken cancellationToken) => Task.CompletedTask;
}
