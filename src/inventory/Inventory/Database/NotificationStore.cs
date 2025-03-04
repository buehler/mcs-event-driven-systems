namespace Inventory.Database;

public class NotificationStore
{
    private readonly List<Notification> _notifications =
    [
        new("System Initialized", DateTime.Now, "The warehouse system is now online."),
    ];

    public IReadOnlyList<Notification> Notifications => _notifications;

    public void Add(Notification notification) => _notifications.Add(notification);

    public void Clear() => _notifications.Clear();

    public void Remove(Notification notification) => _notifications.Remove(notification);
}
