'use client';

import type { Notification } from '@/lib/types';
import { useEffect } from 'react';
import { toast } from 'sonner';

export default function NotificationToasts() {
  useEffect(() => {
    const eventSource = new EventSource('/api/notifications/sse');

    eventSource.onmessage = (event) => {
      const notification = JSON.parse(event.data) as Notification;
      toast(notification.title, {
        description: notification.details,
      });
    };

    return () => {
      eventSource.close();
    };
  });

  return <></>;
}
