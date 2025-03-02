import type { Block as BlockType } from '@/lib/types';
import { Block, EmptyBlock } from './block';
import { Card, CardContent } from './ui/card';

interface BlockStackProps {
  color: BlockType['color'];
  blocks: BlockType[];
  maxBlocks: number;
}

type DefectBlockStackProps = Pick<BlockStackProps, 'blocks'>;

export function BlockStack({ color, blocks, maxBlocks }: BlockStackProps) {
  const emptySlots = Math.max(0, maxBlocks - blocks.length);

  return (
    <Card>
      <CardContent className="p-4 h-full flex flex-col">
        <h3 className="font-medium mb-2 capitalize grow">{color} Blocks</h3>
        <div className="bg-gray-100 rounded-md p-2 flex flex-col space-y-2">
          {Array.from({ length: emptySlots }).map((_, index) => (
            <div key={`empty-${index}`} className="w-full h-8">
              <EmptyBlock />
            </div>
          ))}
          {blocks.map((block, index) => (
            <div key={`empty-${index}`} className="w-full h-8">
              <Block color={block.color} />
            </div>
          ))}
        </div>
        <div className="mt-2 text-center">
          <span className="text-sm font-medium">
            {blocks.length}/{maxBlocks}
          </span>
        </div>
      </CardContent>
    </Card>
  );
}

export function DefectBlockStack({ blocks }: DefectBlockStackProps) {
  return (
    <Card>
      <CardContent>hello</CardContent>
    </Card>
  );
}
