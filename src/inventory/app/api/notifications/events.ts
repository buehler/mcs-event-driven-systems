import type { Notification } from '@/lib/types';
import { eventEmitter } from './sse/route';

const notifications: Notification[] = [
  {
    title: 'System Initialized',
    timestamp: Date.now(),
    details: 'The warehouse system is now online.',
  },
];

export function sendNotification(notification: Notification) {
  console.log(`Send notification: ${notification.title}`);
  notifications.push(notification);
  eventEmitter.emit('event', JSON.stringify(notification));
}

export function getAllNotifications() {
  return notifications;
}
