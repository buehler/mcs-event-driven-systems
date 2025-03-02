'use server';

import type { Warehouse } from './types';

const warehouseState: Warehouse = {
  red: {
    blocks: [
      {
        id: '1',
        color: 'red',
      },
    ],
  },
  blue: { blocks: [] },
  yellow: { blocks: [] },
  green: { blocks: [] },
  defects: { blocks: [] },
};

export async function getWarehouseState() {
  return warehouseState;
}
