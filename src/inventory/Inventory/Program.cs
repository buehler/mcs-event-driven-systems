using Confluent.Kafka;

using Inventory.Components;
using Inventory.Config;
using Inventory.Database;
using Inventory.Kafka;
using Inventory.Kafka.Listener;
using Inventory.Proto.Commands.Inventory.V1;

using MudBlazor.Services;

var builder = WebApplication.CreateBuilder(args);

var config = builder.Configuration.GetRequiredSection("Kafka").Get<KafkaSettings>() ??
             throw new ApplicationException("Config not parsed.");
builder.Services.AddSingleton(config);

builder.Services.AddSingleton<KafkaFactory>();

builder.AddKafkaConsumer<string, AddToInventory>("commands", settings =>
{
    settings.Config.BootstrapServers = string.Join(',', config.Servers);
    settings.Config.GroupId = "inventory-commands";
    settings.Config.AutoOffsetReset = AutoOffsetReset.Latest;
});

builder.Services.AddHostedService<AddToInventoryListener>();

builder.Services.AddRazorComponents()
    .AddInteractiveServerComponents();
builder.Services.AddMudServices();
builder.Services.AddResponseCompression();

builder.Services.AddSingleton<BlockStore>();
builder.Services.AddSingleton<NotificationStore>();

var app = builder.Build();

// Configure the HTTP request pipeline.
if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Error", createScopeForErrors: true);
}


app.UseAntiforgery();
app.UseResponseCompression();

app.MapStaticAssets();
app.MapRazorComponents<App>()
    .AddInteractiveServerRenderMode();

await app.RunAsync();
