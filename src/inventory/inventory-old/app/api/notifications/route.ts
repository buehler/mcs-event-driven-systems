import { getAllNotifications } from '@/lib/events';

export async function GET() {
  return Response.json(getAllNotifications());
}
