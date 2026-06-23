// Service Worker for handling push notifications
self.addEventListener('push', (event) => {
  if (!event.data) {
    console.log('Push event without data');
    return;
  }

  let data = null;
  try {
    data = event.data.json();
  } catch (err) {
    console.warn('Push payload is not valid JSON, falling back to text payload:', err);
    data = {
      title: 'Alerta',
      message: event.data.text() || 'Tienes una nueva alerta',
    };
  }

  const options = {
    body: data.message || data.body || 'Nueva notificación',
    icon: '/favicon.ico',
    badge: '/favicon.ico',
    tag: 'alert-notification',
    requireInteraction: false,
  };

  event.waitUntil(
    self.registration.showNotification(data.title || 'Alerta', options)
  );
});

// Handle notification clicks
self.addEventListener('notificationclick', (event) => {
  event.notification.close();
  event.waitUntil(
    clients.matchAll({ type: 'window', includeUncontrolled: true }).then((clientList) => {
      // Check if app window already exists
      for (const client of clientList) {
        if (client.url === '/' && 'focus' in client) {
          return client.focus();
        }
      }
      // Open the app if no window is found
      if (clients.openWindow) {
        return clients.openWindow('/');
      }
    })
  );
});
