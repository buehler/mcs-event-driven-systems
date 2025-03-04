namespace Inventory.Database;

public enum BlockColor
{
    Red,
    Blue,
    Yellow,
    Green,
}

public record Block(string Id, BlockColor Color, bool IsDefect = false);
