import { useEffect, useRef, useState } from 'react';
import { registerPushSubscription, unregisterPushSubscription, PushSubscriptionDTO } from '../services/api';

const VAPID_PUBLIC_KEY = import.meta.env.VITE_VAPID_PUBLIC_KEY || '';

export interface PushNotificationState {
  isSupported: boolean;
  isSubscribed: boolean;
  permission: NotificationPermission;
  isLoading: boolean;
  statusMessage: string;
  subscribe: () => Promise<boolean>;
  unsubscribe: () => Promise<boolean>;
}

export function usePushNotifications(): PushNotificationState {
  const [isSupported, setIsSupported] = useState(true);
  const [isSubscribed, setIsSubscribed] = useState(false);
  const [permission, setPermission] = useState<NotificationPermission>(Notification.permission);
  const [isLoading, setIsLoading] = useState(true);
  const [statusMessage, setStatusMessage] = useState('Inicializando notificaciones...');
  const serviceWorkerRegistration = useRef<ServiceWorkerRegistration | null>(null);

  useEffect(() => {
    if (!('serviceWorker' in navigator) || !('PushManager' in window)) {
      setIsSupported(false);
      setStatusMessage('El navegador no soporta notificaciones push');
      setIsLoading(false);
      return;
    }

    if (!VAPID_PUBLIC_KEY) {
      setStatusMessage('La clave pública VAPID no está configurada');
      setIsLoading(false);
      return;
    }

    const initialize = async () => {
      try {
        const registration = await navigator.serviceWorker.register('/service-worker.js', {
          scope: '/',
        });
        serviceWorkerRegistration.current = registration;
        setStatusMessage('Service worker registrado');

        const readyRegistration = await navigator.serviceWorker.ready;
        serviceWorkerRegistration.current = readyRegistration;
        setPermission(Notification.permission);

        const existingSubscription = await readyRegistration.pushManager.getSubscription();
        if (existingSubscription) {
          setIsSubscribed(true);
          setStatusMessage('Suscripción push detectada');
          await registerBackendSubscription(existingSubscription);
        } else {
          setIsSubscribed(false);
          setStatusMessage('Presiona el botón para activar notificaciones push');
        }
      } catch (error) {
        console.error('Error initializing push notifications:', error);
        setStatusMessage('Error al inicializar notificaciones push');
      } finally {
        setIsLoading(false);
      }
    };

    initialize();
  }, []);

  const getUserRegionAndCommune = () => {
    const storedUser = localStorage.getItem('user');
    if (!storedUser) {
      return { region: '', commune: '' };
    }

    try {
      const user = JSON.parse(storedUser);
      return {
        region: user.region || '',
        commune: user.commune || '',
      };
    } catch (error) {
      console.error('Error parsing stored user', error);
      return { region: '', commune: '' };
    }
  };

  const registerBackendSubscription = async (subscription: PushSubscription) => {
    const { region, commune } = getUserRegionAndCommune();
    const p256dh = subscription.getKey('p256dh');
    const auth = subscription.getKey('auth');

    const subscriptionObject: PushSubscriptionDTO = {
      endpoint: subscription.endpoint,
      p256dh: arrayBufferToBase64Url(p256dh as any),
      auth: arrayBufferToBase64Url(auth as any),
      region,
      commune,
    };

    const success = await registerPushSubscription(subscriptionObject);
    if (success) {
      localStorage.setItem('pushSubscription', JSON.stringify(subscriptionObject));
    }
    return success;
  };

  const requestNotificationPermission = async () => {
    if (Notification.permission === 'granted') {
      return true;
    }
    if (Notification.permission === 'denied') {
      return false;
    }

    const permissionResult = await Notification.requestPermission();
    setPermission(permissionResult);
    return permissionResult === 'granted';
  };

  const subscribe = async () => {
    if (!serviceWorkerRegistration.current) {
      setStatusMessage('Service worker no está disponible');
      return false;
    }

    setIsLoading(true);
    try {
      const granted = await requestNotificationPermission();
      if (!granted) {
        setStatusMessage('Permiso de notificaciones no concedido');
        return false;
      }

      const existingSubscription = await serviceWorkerRegistration.current.pushManager.getSubscription();
      if (existingSubscription) {
        const registered = await registerBackendSubscription(existingSubscription);
        if (registered) {
          setIsSubscribed(true);
          setStatusMessage('Suscripción push activada');
          return true;
        }
      }

      const subscription = await serviceWorkerRegistration.current.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: urlBase64ToUint8Array(VAPID_PUBLIC_KEY) as any,
      });

      const registered = await registerBackendSubscription(subscription);
      if (registered) {
        setIsSubscribed(true);
        setStatusMessage('Suscripción push activada');
        return true;
      }

      setStatusMessage('No se pudo registrar la suscripción en el backend');
      return false;
    } catch (error) {
      console.error('Error during push subscribe:', error);
      setStatusMessage('Error al suscribir a notificaciones push');
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const unsubscribe = async () => {
    if (!serviceWorkerRegistration.current) {
      setStatusMessage('Service worker no está disponible');
      return false;
    }

    setIsLoading(true);
    try {
      const subscription = await serviceWorkerRegistration.current.pushManager.getSubscription();
      if (!subscription) {
        setIsSubscribed(false);
        setStatusMessage('No existe suscripción activa');
        return true;
      }

      const endpoint = subscription.endpoint;
      const unsubscribed = await subscription.unsubscribe();
      await unregisterPushSubscription(endpoint);
      localStorage.removeItem('pushSubscription');

      if (unsubscribed) {
        setIsSubscribed(false);
        setStatusMessage('Suscripción push deshabilitada');
        return true;
      }

      setIsSubscribed(false);
      setStatusMessage('No se pudo cancelar la suscripción');
      return false;
    } catch (error) {
      console.error('Error during push unsubscribe:', error);
      setStatusMessage('Error al desuscribir de notificaciones push');
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  return {
    isSupported,
    isSubscribed,
    permission,
    isLoading,
    statusMessage,
    subscribe,
    unsubscribe,
  };
}

// Helper function to convert VAPID public key
function urlBase64ToUint8Array(base64String: string): Uint8Array {
  const padding = '='.repeat((4 - (base64String.length % 4)) % 4);
  const base64 = (base64String + padding).replace(/\-/g, '+').replace(/_/g, '/');
  const rawData = window.atob(base64);
  const outputArray = new Uint8Array(rawData.length);
  for (let i = 0; i < rawData.length; ++i) {
    outputArray[i] = rawData.charCodeAt(i);
  }
  return outputArray;
}

// Helper function to convert ArrayBuffer to base64
function arrayBufferToBase64Url(buffer: any): string {
  let bytes: Uint8Array;
  
  if (buffer instanceof Uint8Array) {
    bytes = buffer;
  } else if (buffer instanceof ArrayBuffer) {
    bytes = new Uint8Array(buffer);
  } else {
    bytes = new Uint8Array(buffer);
  }
  
  let binary = '';
  for (let i = 0; i < bytes.byteLength; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  const base64 = window.btoa(binary);
  return base64.replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

function arrayBufferToBase64(buffer: any): string {
  let bytes: Uint8Array;
  
  if (buffer instanceof Uint8Array) {
    bytes = buffer;
  } else if (buffer instanceof ArrayBuffer) {
    bytes = new Uint8Array(buffer);
  } else {
    bytes = new Uint8Array(buffer);
  }
  
  let binary = '';
  for (let i = 0; i < bytes.byteLength; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return window.btoa(binary);
}
