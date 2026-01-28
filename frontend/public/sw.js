// Simple service worker for basic PWA support during development.
// Caches the shell and serves it when offline. Keep this minimal to avoid
// aggressive caching issues while iterating.

const CACHE_NAME = 'linkclassify-cache-v1';
const OFFLINE_URLS = ['/', '/import-link'];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => cache.addAll(OFFLINE_URLS))
  );
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(
        keys
          .filter((key) => key !== CACHE_NAME)
          .map((key) => caches.delete(key))
      )
    )
  );
  self.clients.claim();
});

self.addEventListener('fetch', (event) => {
  const { request } = event;

  // Only handle GET requests; let others pass through
  if (request.method !== 'GET') {
    return;
  }

  event.respondWith(
    caches.match(request).then((cached) => {
      if (cached) {
        return cached;
      }
      return fetch(request).catch(() => {
        // If offline and request is navigation, fall back to cached root
        if (request.mode === 'navigate') {
          return caches.match('/');
        }
        throw new Error('Network error and no cache available');
      });
    })
  );
});

