import { getNotificationEventEmitter } from '@/lib/events';

export async function GET(request: Request) {
  const stream = new ReadableStream({
    start(controller) {
      const eventEmitter = getNotificationEventEmitter();

      const onMessage = (data: string) => {
        controller.enqueue(`data: ${data}\n\n`);
      };

      eventEmitter.on('event', onMessage);

      request.signal.addEventListener('abort', () => {
        eventEmitter.off('event', onMessage);
      });
    },
  });

  return new Response(stream, {
    headers: {
      'Content-Type': 'text/event-stream',
      'Cache-Control': 'no-cache',
      Connection: 'keep-alive',
      'Access-Control-Allow-Origin': '*',
    },
  });
}
