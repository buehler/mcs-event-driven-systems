import NotificationArea from '@/components/notification-area';
import NotificationSkeleton from '@/components/notification-skeleton';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import Warehouse from '@/components/warehouse-view';
import { Plus } from 'lucide-react';
import Link from 'next/link';
import { Suspense } from 'react';

export default function Home() {
  return (
    <div className="container mx-auto py-8">
      <h1 className="text-3xl font-bold mb-8">Warehouse Management System</h1>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2">
          <Card>
            <CardHeader>
              <CardTitle>Current Warehouse State</CardTitle>
              <CardDescription>Overview of block inventory and storage</CardDescription>
            </CardHeader>
            <CardContent>
              <Suspense fallback={<div>Loading warehouse state...</div>}>
                <Warehouse />
              </Suspense>
            </CardContent>
          </Card>
        </div>

        <div>
          <Card className="mb-8">
            <CardHeader className="pb-3">
              <CardTitle>Actions</CardTitle>
            </CardHeader>
            <CardContent>
              <Link href="/delivery">
                <Button className="w-full">
                  <Plus className="mr-2 h-4 w-4" />
                  Create Delivery Notice
                </Button>
              </Link>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Notifications</CardTitle>
              <CardDescription>Real-time system updates</CardDescription>
            </CardHeader>
            <CardContent>
              <Suspense fallback={<NotificationSkeleton />}>
                <NotificationArea />
              </Suspense>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
