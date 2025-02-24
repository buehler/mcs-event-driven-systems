using Confluent.Kafka;

namespace Consumer.Consumers;

public class Clock(ILogger<Clock> logger, IConsumer<string, long> consumer) : IHostedService
{
    private const string Topic = "clock";
    private Task? _backgroundTask;
    private CancellationTokenSource? _cts;

    public Task StartAsync(CancellationToken cancellationToken)
    {
        logger.LogInformation("Kafka Consumer started.");
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
        logger.LogInformation("Kafka Consumer stopped.");
    }

    private Task RunAsync(CancellationToken token)
    {
        consumer.Subscribe(Topic);

        try
        {
            while (!token.IsCancellationRequested)
            {
                var result = consumer.Consume(token);
                logger.LogInformation(
                    "Consumed message on topic '{Topic}' '{Key}': {Value}",
                    result.Topic,
                    result.Message.Key,
                    result.Message.Value);
            }
        }
        catch (OperationCanceledException)
        {
            logger.LogDebug("Cancellation of consumer loop requested.");
        }
        finally
        {
            consumer.Unsubscribe();
        }

        return Task.CompletedTask;
    }
}
