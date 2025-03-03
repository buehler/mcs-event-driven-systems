import { getWarehouseState } from '@/lib/actions';
import { BlockStack, DefectBlockStack } from './block-stack';

export default async function WarehouseView() {
  const warehouseState = await getWarehouseState();

  return (
    <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
      <BlockStack color="red" blocks={warehouseState.red.blocks} maxBlocks={5} />
      <BlockStack color="blue" blocks={warehouseState.blue.blocks} maxBlocks={5} />
      <BlockStack color="yellow" blocks={warehouseState.yellow.blocks} maxBlocks={5} />
      <BlockStack color="green" blocks={warehouseState.green.blocks} maxBlocks={5} />
      <DefectBlockStack blocks={warehouseState.defects.blocks} />
    </div>
  );
}
