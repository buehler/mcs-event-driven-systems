using Producer.Config;
using Producer.Producers;

var builder = Host.CreateApplicationBuilder(args);

var config = builder.Configuration.GetRequiredSection("Kafka").Get<KafkaSettings>() ??
             throw new ApplicationException("Config not parsed.");
builder.Services.AddSingleton(config);

builder.AddKafkaProducer<string, long>("clock", settings =>
{
    settings.Config.BootstrapServers = string.Join(',', config.Servers);
    settings.Config.MessageSendMaxRetries = config.Retries;
});
builder.Services.AddHostedService<Clock>();

using var host = builder.Build();
await host.RunAsync();
