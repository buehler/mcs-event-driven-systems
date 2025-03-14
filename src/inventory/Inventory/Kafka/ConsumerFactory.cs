using Confluent.Kafka;

using Google.Protobuf;

using Inventory.Config;

namespace Inventory.Kafka;

public class ConsumerFactory(IServiceProvider serviceProvider)
{
    public IConsumer<string, T> CreateConsumer<T>(string groupId, AutoOffsetReset autoOffsetReset) where T : IMessage<T>
    {
        var settings = serviceProvider.GetRequiredService<KafkaSettings>();
        var config = new ConsumerConfig
        {
            BootstrapServers = string.Join(',', settings.Servers),
            GroupId = groupId,
            AutoOffsetReset = autoOffsetReset,
        };

        var builder = new ConsumerBuilder<string, T>(config);
        switch (typeof(T).GetProperty("Parser")?.GetValue(null))
        {
            case MessageParser<T> parser:
                builder.SetValueDeserializer(new ProtobufDeserializer<T>(parser));
                break;
            default:
                throw new InvalidOperationException("No parser found for type.");
        }

        return builder.Build();
    }

    private class ProtobufDeserializer<T>(MessageParser<T> parser) : IDeserializer<T> where T : IMessage<T>
    {
        public T Deserialize(ReadOnlySpan<byte> data, bool isNull, SerializationContext context) =>
            isNull ? default! : parser.ParseFrom(data);
    }
}
