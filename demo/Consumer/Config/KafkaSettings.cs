using Confluent.Kafka;

namespace Consumer.Config;

public sealed class KafkaSettings
{
    public required string[] Servers { get; init; }
    public required string GroupId { get; init; }
    public required AutoOffsetReset AutoOffsetReset { get; init; }
}
