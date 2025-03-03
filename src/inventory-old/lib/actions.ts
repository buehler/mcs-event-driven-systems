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
  blue: {
    blocks: [
      {
        id: '2',
        color: 'blue',
      },
    ],
  },
  yellow: {
    blocks: [
      {
        id: '3',
        color: 'yellow',
      },
    ],
  },
  green: {
    blocks: [
      {
        id: '4',
        color: 'green',
      },
    ],
  },
  defects: {
    blocks: [
      {
        id: '5',
        color: 'red',
      },
      {
        id: '6',
        color: 'red',
      },
      {
        id: '7',
        color: 'blue',
      },
      {
        id: '8',
        color: 'yellow',
      },
      {
        id: '9',
        color: 'green',
      },
      {
        id: '10',
        color: 'green',
      },
      {
        id: '11',
        color: 'green',
      },
      {
        id: '12',
        color: 'yellow',
      },
      {
        id: '5',
        color: 'red',
      },
      {
        id: '6',
        color: 'red',
      },
      {
        id: '7',
        color: 'blue',
      },
      {
        id: '8',
        color: 'yellow',
      },
      {
        id: '9',
        color: 'green',
      },
      {
        id: '10',
        color: 'green',
      },
      {
        id: '11',
        color: 'green',
      },
      {
        id: '12',
        color: 'yellow',
      },
      {
        id: '5',
        color: 'red',
      },
      {
        id: '6',
        color: 'red',
      },
      {
        id: '7',
        color: 'blue',
      },
      {
        id: '8',
        color: 'yellow',
      },
      {
        id: '9',
        color: 'green',
      },
      {
        id: '10',
        color: 'green',
      },
      {
        id: '11',
        color: 'green',
      },
      {
        id: '12',
        color: 'yellow',
      },
    ],
  },
};

export async function getWarehouseState() {
  return warehouseState;
}
