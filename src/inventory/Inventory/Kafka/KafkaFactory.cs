using System.Text;

using Confluent.Kafka;

using Google.Protobuf;

using Inventory.Config;

namespace Inventory.Kafka;

public class KafkaFactory(IServiceProvider serviceProvider)
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

    public IProducer<string, T> CreateProducer<T>() where T : IMessage<T>
    {
        var settings = serviceProvider.GetRequiredService<KafkaSettings>();
        var config = new ProducerConfig
        {
            BootstrapServers = string.Join(',', settings.Servers), MessageSendMaxRetries = 3,
        };

        var builder = new ProducerBuilder<string, T>(config);
        builder.SetValueSerializer(new ProtobufSerializer<T>());

        return builder.Build();
    }

    public async Task SendMessage<T>(string topic, T data) where T : IMessage<T>
    {
        using var producer = CreateProducer<T>();
        await producer.ProduceAsync(topic,
            new Message<string, T>
            {
                Value = data, Headers = [new Header("messageType", Encoding.UTF8.GetBytes(typeof(T).Name))],
            });
        producer.Flush(TimeSpan.FromMilliseconds(500));
    }

    private class ProtobufDeserializer<T>(MessageParser<T> parser) : IDeserializer<T> where T : IMessage<T>
    {
        public T Deserialize(ReadOnlySpan<byte> data, bool isNull, SerializationContext context) =>
            isNull ? default! : parser.ParseFrom(data);
    }

    private class ProtobufSerializer<T> : ISerializer<T> where T : IMessage<T>
    {
        public byte[] Serialize(T data, SerializationContext context) => data.ToByteArray();
    }
}
