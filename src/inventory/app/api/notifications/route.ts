import { getAllNotifications } from './events';

export async function GET() {
  return Response.json(getAllNotifications());
}
