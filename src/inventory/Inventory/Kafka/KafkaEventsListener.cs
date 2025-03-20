using System.Text;

using Confluent.Kafka;

using Google.Protobuf;

using Inventory.Config;

namespace Inventory.Kafka;

public sealed class KafkaEventsListener(KafkaSettings settings)
{
    private const string Topic = "events";
    private Task? _backgroundTask;
    private CancellationTokenSource? _cts;
    private readonly Dictionary<string, List<Func<byte[], Task>>> _handlers = new();

    public void AddHandler<T>(Func<T, Task> handler) where T : IMessage<T>
    {
        var messageType = typeof(T).Name;
        if (!_handlers.TryGetValue(messageType, out var handlers))
        {
            handlers = new();
            _handlers.Add(messageType, handlers);
        }

        if (typeof(T).GetProperty("Parser")?.GetValue(null) is MessageParser<T> parser)
        {
            handlers.Add(async message =>
            {
                var parsedMessage = parser.ParseFrom(message);
                await handler(parsedMessage);
            });
        }
    }

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

    private async Task RunAsync(CancellationToken token)
    {
        var config = new ConsumerConfig
        {
            BootstrapServers = string.Join(',', settings.Servers),
            GroupId = "inventory-events",
            AutoOffsetReset = AutoOffsetReset.Latest,
        };
        var builder = new ConsumerBuilder<string, byte[]>(config);
        builder.SetValueDeserializer(Deserializers.ByteArray);

        using var consumer = builder.Build();
        consumer.Subscribe(Topic);

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

                if (!_handlers.TryGetValue(messageType, out var handlers))
                {
                    continue;
                }

                foreach (var handler in handlers)
                {
                    await handler(result.Message.Value);
                }
            }
        }
        finally
        {
            consumer.Unsubscribe();
        }
    }
}
