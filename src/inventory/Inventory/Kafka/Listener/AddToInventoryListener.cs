using Confluent.Kafka;

using Inventory.Database;
using Inventory.Proto.Commands.Inventory.V1;

using BlockColor = Inventory.Proto.Commands.Inventory.V1.BlockColor;

namespace Inventory.Kafka.Listener;

public class AddToInventoryListener(
    ConsumerFactory factory,
    ILogger<AddToInventoryListener> logger,
    NotificationStore notifications,
    BlockStore blocks)
    : BaseListener<AddToInventory>(factory, "commands", "inventory-commands", AutoOffsetReset.Latest)
{
    protected override Task Handle(AddToInventory message)
    {
        logger.LogInformation($"Received add to inventory {message} command");
        notifications.Add(new($"Add new Block: {message.Color}"));
        blocks.Add(new(message.Color switch
        {
            BlockColor.Red => Database.BlockColor.Red,
            BlockColor.Blue => Database.BlockColor.Blue,
            BlockColor.Yellow => Database.BlockColor.Yellow,
            BlockColor.Green => Database.BlockColor.Green,
            _ => throw new ArgumentOutOfRangeException(),
        }));

        return Task.CompletedTask;
    }
}
