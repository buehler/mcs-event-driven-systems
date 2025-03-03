import { Button } from '@/components/ui/button';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { ArrowLeft } from 'lucide-react';
import Link from 'next/link';

export default function Delivery() {
  return (
    <div className="container mx-auto py-8">
      <Link href="/">
        <Button variant="ghost" className="mb-4">
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Dashboard
        </Button>
      </Link>

      <Card>
        <CardHeader>
          <CardTitle>Create Delivery Notice</CardTitle>
          <CardDescription>Specify the colors of blocks in the new delivery</CardDescription>
        </CardHeader>
        <CardContent>FORM</CardContent>
      </Card>
    </div>
  );
}
