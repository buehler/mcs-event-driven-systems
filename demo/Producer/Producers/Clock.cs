using Confluent.Kafka;

using Producer.Messages;

namespace Producer.Producers;

public class Clock(ILogger<Clock> logger, IProducer<string, long> producer) : IHostedService, IDisposable
{
    private const string Topic = "clock";
    private Timer? _timer;

    public Task StartAsync(CancellationToken cancellationToken)
    {
        _timer = new Timer(Callback, null, TimeSpan.FromSeconds(1), TimeSpan.FromSeconds(1));
        logger.LogInformation("Kafka Producer started.");

        return Task.CompletedTask;
    }

    public Task StopAsync(CancellationToken cancellationToken)
    {
        _timer?.Change(Timeout.Infinite, 0);
        logger.LogInformation("Kafka Producer stopped.");

        return Task.CompletedTask;
    }
    
    private async void Callback(object? state)
    {
        try
        {
            logger.LogDebug("Producing message.");
            var msg = new ClockEvent();
            await producer.ProduceAsync(Topic, msg);
        }
        catch (Exception e)
        {
            logger.LogError(e, "Error during publishing / producing a message.");
        }
    }

    public void Dispose() => _timer?.Dispose();
}
