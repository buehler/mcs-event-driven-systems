using System.Text;

using Confluent.Kafka;

using Google.Protobuf;

using Inventory.Config;

namespace Inventory.Kafka;

public class KafkaFactory(IServiceProvider serviceProvider)
{
    private IProducer<string, T> CreateProducer<T>() where T : IMessage<T>
    {
        var settings = serviceProvider.GetRequiredService<KafkaSettings>();
        var config = new ProducerConfig
        {
            BootstrapServers = string.Join(',', settings.Servers),
            MessageSendMaxRetries = 3,
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
                Value = data,
                Headers = [new Header("messageType", Encoding.UTF8.GetBytes(typeof(T).Name))],
            });
        producer.Flush(TimeSpan.FromMilliseconds(500));
    }

    private class ProtobufSerializer<T> : ISerializer<T> where T : IMessage<T>
    {
        public byte[] Serialize(T data, SerializationContext context) => data.ToByteArray();
    }
}
