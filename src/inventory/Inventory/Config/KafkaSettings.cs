namespace Inventory.Config;

public sealed class KafkaSettings
{
    public required string[] Servers { get; init; }
}
