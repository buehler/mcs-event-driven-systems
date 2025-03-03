'use client';

import type { Notification } from '@/lib/types';
import { useEffect, useState } from 'react';
import { ScrollArea } from './ui/scroll-area';

export default function NotificationArea() {
  const [notifications, setNotifications] = useState<Notification[]>([]);

  useEffect(() => {
    const eventSource = new EventSource('/api/notifications/sse');

    eventSource.onmessage = (event) => {
      const notification = JSON.parse(event.data) as Notification;
      setNotifications((prevNotifications) => [notification, ...prevNotifications]);
    };

    return () => {
      eventSource.close();
    };
  });

  useEffect(() => {
    fetch('/api/notifications')
      .then((res) => res.json())
      .then((data) => {
        setNotifications(data);
      });
  }, []);

  return (
    <ScrollArea className="h-96">
      {notifications.length === 0 ? (
        <div className="text-center py-8 text-muted-foreground">No notifications yet</div>
      ) : (
        <div className="space-y-4">
          {notifications.map((notification, index) => (
            <div key={index} className="p-3 border rounded-lg bg-background">
              <div className="flex justify-between items-start mb-1">
                <span className="font-medium">{notification.title}</span>
                <span className="text-xs text-muted-foreground">
                  {new Date(notification.timestamp).toLocaleTimeString()}
                </span>
              </div>
              {notification.details && <p className="text-sm text-muted-foreground">{notification.details}</p>}
            </div>
          ))}
        </div>
      )}
    </ScrollArea>
  );
}
