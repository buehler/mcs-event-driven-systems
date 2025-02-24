using Confluent.Kafka;

namespace Producer.Messages;

public struct ClockEvent()
{
    private string Key { get; } = Guid.NewGuid().ToString();
    private long Timestamp { get; } = DateTimeOffset.UtcNow.ToUnixTimeSeconds();

    public static implicit operator Message<string, long>(ClockEvent clockEvent) =>
        new() { Key = clockEvent.Key, Value = clockEvent.Timestamp };
}
