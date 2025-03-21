namespace Inventory.Kafka;

public class KafkaListenerService(KafkaEventsListener listener) : IHostedService
{
    public Task StartAsync(CancellationToken cancellationToken)
        => listener.StartAsync(cancellationToken);

    public Task StopAsync(CancellationToken cancellationToken)
        => listener.StopAsync(cancellationToken);
}
