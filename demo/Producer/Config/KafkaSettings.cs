namespace Producer.Config;

public sealed class KafkaSettings
{
    public required string[] Servers { get; init; }
    public required int Retries { get; init; }
}
