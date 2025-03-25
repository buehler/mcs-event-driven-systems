using System.Text;

using Confluent.Kafka;

using Inventory.Config;
using Inventory.Database;
using Inventory.Proto.Commands.Inventory.V1;
using Inventory.Proto.Commands.Machines.V1;
using Inventory.Proto.Events.Inventory.V1;
using Inventory.Proto.Events.Machines.V1;
using Inventory.Proto.Events.Sensors.V1;

namespace Inventory.Kafka;

public sealed class KafkaAllMessageListener(
    ILogger<KafkaAllMessageListener> logger,
    KafkaSettings settings,
    MessageStore store) : IHostedService
{
    private const string CommandTopic = "commands";
    private const string EventTopic = "events";

    private Task? _backgroundTask;
    private CancellationTokenSource? _cts;

    public Task StartAsync(CancellationToken cancellationToken)
    {
        _cts = CancellationTokenSource.CreateLinkedTokenSource(cancellationToken);
        _backgroundTask = Task.Run(() => RunAsync(_cts.Token), CancellationToken.None);

        return Task.CompletedTask;
    }

    public async Task StopAsync(CancellationToken cancellationToken)
    {
        if (_cts is not null)
        {
            await _cts.CancelAsync();
        }

        if (_backgroundTask is not null)
        {
            await Task.WhenAny(_backgroundTask, Task.Delay(Timeout.Infinite, cancellationToken));
        }

        _cts?.Dispose();
    }

    private Task RunAsync(CancellationToken token)
    {
        var config = new ConsumerConfig
        {
            BootstrapServers = string.Join(',', settings.Servers),
            GroupId = "inventory-all-messages-listener",
            AutoOffsetReset = AutoOffsetReset.Latest,
        };
        var builder = new ConsumerBuilder<string, byte[]>(config);
        builder.SetValueDeserializer(Deserializers.ByteArray);

        logger.LogInformation("Start Kafka Listener.");
        using var consumer = builder.Build();
        consumer.Subscribe([CommandTopic, EventTopic]);

        try
        {
            while (!token.IsCancellationRequested)
            {
                var result = consumer.Consume(token);
                if (!result.Message.Headers.TryGetLastBytes("messageType", out var data) ||
                    Encoding.UTF8.GetString(data ?? []) is not { } messageType)
                {
                    continue;
                }

                logger.LogDebug("Got message of type {MessageType}.", messageType);
                switch (messageType)
                {
                    case nameof(ProcessNewShipment):
                        {
                            var obj = ProcessNewShipment.Parser.ParseFrom(result.Message.Value);
                            store.Add(new(nameof(ProcessNewShipment),
                                $"Process {obj.ShipmentId} with {obj.Blocks.Count(b => b)} blocks."));
                            break;
                        }
                    case nameof(ShipmentProcessed):
                        {
                            var obj = ShipmentProcessed.Parser.ParseFrom(result.Message.Value);
                            store.Add(new(nameof(ShipmentProcessed),
                                $"Processed {obj.ShipmentId}."));
                            break;
                        }
                    case nameof(MoveBlockFromShipmentToNfc):
                        {
                            var obj = MoveBlockFromShipmentToNfc.Parser.ParseFrom(result.Message.Value);
                            store.Add(new(nameof(MoveBlockFromShipmentToNfc),
                                $"Move block from {obj.Position} to NFC sensor."));
                            break;
                        }
                    case nameof(SortBlock):
                        {
                            var obj = SortBlock.Parser.ParseFrom(result.Message.Value);
                            store.Add(new(nameof(SortBlock),
                                $"Sort {obj.Color} block"));
                            break;
                        }
                    case nameof(BlockSorted):
                        {
                            var obj = BlockSorted.Parser.ParseFrom(result.Message.Value);
                            store.Add(new(nameof(BlockSorted),
                                $"Sorted {obj.Color} block"));
                            break;
                        }
                    case nameof(ConveyorSpeedChanged):
                        {
                            var obj = ConveyorSpeedChanged.Parser.ParseFrom(result.Message.Value);
                            store.Add(new(nameof(ConveyorSpeedChanged),
                                $"New conveyor speed rotary value: {obj.Speed}."));
                            break;
                        }
                    case nameof(ColorDetected):
                        {
                            var obj = ColorDetected.Parser.ParseFrom(result.Message.Value);
                            store.Add(new(nameof(ColorDetected),
                                $"Detected a {obj.Color} block."));
                            break;
                        }
                    default:
                        store.Add(new(messageType));
                        break;
                }
            }
        }
        catch (Exception e)
        {
            logger.LogError(e, "Error while consuming message.");
        }
        finally
        {
            consumer.Unsubscribe();
        }

        return Task.CompletedTask;
    }
}
