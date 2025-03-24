namespace Inventory.Database;

public sealed class NotificationStore
{
    private readonly List<Notification> _notifications =
    [
        new("System Initialized", DateTime.Now, "The warehouse system is now online."),
    ];

    public event Action? OnChange;

    public IReadOnlyList<Notification> Notifications => _notifications;

    public void Add(Notification notification)
    {
        _notifications.Add(notification);
        OnChange?.Invoke();
    }

    public void Clear()
    {
        _notifications.Clear();
        OnChange?.Invoke();
    }

    public void Remove(Notification notification)
    {
        _notifications.Remove(notification);
        OnChange?.Invoke();
    }
}
