namespace Inventory.Database;

public record Notification(string Title, DateTime Date, string? Detail = null)
{
    public Notification(string title) : this(title, DateTime.Now) { }
}
