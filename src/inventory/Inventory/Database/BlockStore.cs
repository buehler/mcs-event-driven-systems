namespace Inventory.Database;

public class BlockStore
{
    private readonly List<Block> _blocks = new();
    
    public event Action? OnChange;

    public IReadOnlyList<Block> ColoredBlocks(BlockColor color) => _blocks.Where(b => b.Color == color).ToList();
    
    public void Add(Block block)
    {
        _blocks.Add(block);
        OnChange?.Invoke();
    }

    public void Clear()
    {
        _blocks.Clear();
        OnChange?.Invoke();
    }

    public void Remove(Block block)
    {
        _blocks.Remove(block);
        OnChange?.Invoke();
    }
}
