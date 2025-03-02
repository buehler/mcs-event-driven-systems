import { Skeleton } from './ui/skeleton';

export default function NotificationSkeleton() {
  return (
    <div>
      <div className="flex justify-between mb-2">
        <Skeleton className="h-4 w-1/2" />
        <Skeleton className="h-4 w-1/4" />
      </div>
      <Skeleton className="h-4 w-full" />
    </div>
  );
}
