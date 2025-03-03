export interface Notification {
  title: string;
  details?: string;
  timestamp: number;
}

export interface Block {
  id: string;
  color: 'red' | 'blue' | 'yellow' | 'green';
}

export type NewBlock = Omit<Block, 'id'>;

export interface BlockStack {
  blocks: Block[];
}

export interface Warehouse {
  red: BlockStack;
  blue: BlockStack;
  yellow: BlockStack;
  green: BlockStack;
  defects: BlockStack;
}
