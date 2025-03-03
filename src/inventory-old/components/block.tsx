import type { Block } from '@/lib/types';

interface BlockProps {
  color: Block['color'];
}

const colorClasses = {
  red: 'bg-red-500 border-red-800',
  blue: 'bg-blue-500 border-blue-800',
  yellow: 'bg-yellow-500 border-yellow-800',
  green: 'bg-green-500 border-green-800',
};

export function Block({ color }: BlockProps) {
  return <div className={`h-full w-full border-2 ${colorClasses[color]} radius-inherit`}></div>;
}

export function EmptyBlock() {
  return <div className="h-full w-full bg-gray-200 border-2 border-dashed border-gray-400 radius-inherit"></div>;
}
