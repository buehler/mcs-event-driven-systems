using Inventory.Database;

namespace Inventory.Hubs;

public interface INotificationClient
{
    Task ReceiveNotification(Notification notification);
}
