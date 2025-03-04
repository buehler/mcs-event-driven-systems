using Inventory.Database;

using Microsoft.AspNetCore.SignalR;

namespace Inventory.Hubs;

public class NotificationHub : Hub<INotificationClient>
{
    public Task SendNotification(Notification notification) =>
        Clients.All.ReceiveNotification(notification);
}
