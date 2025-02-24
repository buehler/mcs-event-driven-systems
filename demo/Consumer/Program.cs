using Consumer.Config;
using Consumer.Consumers;

var builder = Host.CreateApplicationBuilder(args);

var config = builder.Configuration.GetRequiredSection("Kafka").Get<KafkaSettings>() ??
             throw new ApplicationException("Config not parsed.");
builder.Services.AddSingleton(config);

builder.AddKafkaConsumer<string, long>("clock", settings =>
{
    settings.Config.GroupId = config.GroupId;
    settings.Config.BootstrapServers = string.Join(',', config.Servers);
    settings.Config.AutoOffsetReset = config.AutoOffsetReset;
});
builder.Services.AddHostedService<Clock>();

using var host = builder.Build();
await host.RunAsync();
