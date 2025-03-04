namespace Inventory.Database;

public class BlockStore
{
    private readonly List<Block> _blocks = new()
    {
        new Block("1", BlockColor.Red),
        new Block("1", BlockColor.Red),
        new Block("1", BlockColor.Red),
        new Block("2", BlockColor.Blue),
        new Block("2", BlockColor.Blue),
        new Block("3", BlockColor.Green),
        new Block("4", BlockColor.Yellow),
        new Block("5", BlockColor.Red, true),
        new Block("6", BlockColor.Blue, true),
        new Block("7", BlockColor.Green, true),
        new Block("8", BlockColor.Yellow, true),
    };

    public IReadOnlyList<Block> Red => _blocks.Where(b => b is { IsDefect: false, Color: BlockColor.Red }).ToList();
    
    public IReadOnlyList<Block> Blue => _blocks.Where(b => b is { IsDefect: false, Color: BlockColor.Blue }).ToList();

    public IReadOnlyList<Block> Yellow =>
        _blocks.Where(b => b is { IsDefect: false, Color: BlockColor.Yellow }).ToList();

    public IReadOnlyList<Block> Green => _blocks.Where(b => b is { IsDefect: false, Color: BlockColor.Green }).ToList();
    
    public IReadOnlyList<Block> Defects => _blocks.Where(b => b.IsDefect).ToList();

    public void Add(Block block) => _blocks.Add(block);

    public void Clear() => _blocks.Clear();

    public void Remove(Block block) => _blocks.Remove(block);

    public void Remove(string id) => _blocks.RemoveAll(b => b.Id == id);
}
