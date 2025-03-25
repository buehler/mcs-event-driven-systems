using Inventory.Database;
using Inventory.Proto.Events.Machines.V1;

using BlockColor = Inventory.Proto.Models.V1.BlockColor;

namespace Inventory.Kafka.Listener;

public class BlockSortedListener(
    KafkaEventsListener events,
    ILogger<BlockSortedListener> logger,
    NotificationStore notifications,
    BlockStore blocks)
    : IHostedService
{
    private Task Handle(BlockSorted message)
    {
        logger.LogInformation($"Received block sorted {message} event");
        notifications.Add(new($"Sorted {message.Color} Block"));
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

    public Task StartAsync(CancellationToken cancellationToken)
    {
        events.AddHandler<BlockSorted>(Handle);
        return Task.CompletedTask;
    }

    public  Task StopAsync(CancellationToken cancellationToken) => Task.CompletedTask;
}
