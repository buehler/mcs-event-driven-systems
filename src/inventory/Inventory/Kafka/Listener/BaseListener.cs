using System.Text;

using Confluent.Kafka;

using Google.Protobuf;

namespace Inventory.Kafka.Listener;

public abstract class BaseListener<T>(
    KafkaFactory factory,
    string topic,
    string groupId,
    AutoOffsetReset autoOffsetReset) : IHostedService where T : IMessage<T>
{
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

    protected abstract Task Handle(T message);

    private async Task RunAsync(CancellationToken token)
    {
        using var consumer = factory.CreateConsumer<T>(groupId, autoOffsetReset);
        consumer.Subscribe(topic);

        try
        {
            while (!token.IsCancellationRequested)
            {
                var result = consumer.Consume(token);
                if (result.Message.Headers.TryGetLastBytes("messageType", out var data) &&
                    Encoding.UTF8.GetString(data) == typeof(T).Name)
                {
                    await Handle(result.Message.Value);
                }
            }
        }
        finally
        {
            consumer.Unsubscribe();
        }
    }
}
