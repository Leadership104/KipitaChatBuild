// Kipita Service Worker v4
const CACHE_NAME = 'kipita-v4';
const PRECACHE = ['/', '/index.html', '/styles.css', '/app.js', '/manifest.json'];

// Network-first API domains (always try live, fall back to cache)
const NETWORK_FIRST = [
  'api.coingecko.com',
  'api.btcmap.org',
  'api.open-meteo.com',
  'nominatim.openstreetmap.org',
  'places.googleapis.com',
  'fonts.googleapis.com',
  'fonts.gstatic.com',
];

self.addEventListener('install', e => {
  e.waitUntil(caches.open(CACHE_NAME).then(c => c.addAll(PRECACHE)));
  self.skipWaiting();
});

self.addEventListener('activate', e => {
  e.waitUntil(caches.keys().then(keys =>
    Promise.all(keys.filter(k => k !== CACHE_NAME).map(k => caches.delete(k)))
  ));
  self.clients.claim();
});

self.addEventListener('fetch', e => {
  const url = e.request.url;

  // Network-first for all API calls
  if (NETWORK_FIRST.some(domain => url.includes(domain))) {
    e.respondWith(
      fetch(e.request)
        .then(resp => {
          // Cache successful GET responses for offline fallback
          if (resp.ok && e.request.method === 'GET') {
            const clone = resp.clone();
            caches.open(CACHE_NAME).then(c => c.put(e.request, clone));
          }
          return resp;
        })
        .catch(() => caches.match(e.request))
    );
    return;
  }

  // Cache-first for static assets
  e.respondWith(
    caches.match(e.request).then(cached => cached || fetch(e.request))
  );
});
