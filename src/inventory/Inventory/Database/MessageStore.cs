namespace Inventory.Database;

public sealed class MessageStore
{
    private const int MaxMessages = 32;

    private readonly List<Notification> _notifications = [];

    public event Action? OnChange;

    public IReadOnlyList<Notification> Notifications => _notifications;

    public void Add(Notification notification)
    {
        _notifications.Insert(0, notification);
        if (_notifications.Count > MaxMessages)
        {
            _notifications.RemoveRange(MaxMessages, _notifications.Count - MaxMessages);
        }

        OnChange?.Invoke();
    }
}
