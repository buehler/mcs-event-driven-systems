namespace Inventory.Database;

public enum BlockColor
{
    Red,
    Blue,
    Yellow,
    Green,
}

public record Block(BlockColor Color);
