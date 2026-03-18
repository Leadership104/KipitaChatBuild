// Kipita Desktop SW v1
const CACHE = 'kipita-desktop-v1';
const PRECACHE = ['/', './index.html', './manifest.json'];
const NET_FIRST = ['api.coingecko.com','api.btcmap.org','overpass-api.de','nominatim.openstreetmap.org','generativelanguage.googleapis.com'];

self.addEventListener('install', e => {
  e.waitUntil(caches.open(CACHE).then(c => c.addAll(PRECACHE).catch(() => {})));
  self.skipWaiting();
});
self.addEventListener('activate', e => {
  e.waitUntil(caches.keys().then(keys => Promise.all(keys.filter(k => k !== CACHE).map(k => caches.delete(k)))));
  self.clients.claim();
});
self.addEventListener('fetch', e => {
  const url = e.request.url;
  if (NET_FIRST.some(d => url.includes(d))) {
    e.respondWith(fetch(e.request).then(r => {
      if (r.ok && e.request.method === 'GET') caches.open(CACHE).then(c => c.put(e.request, r.clone()));
      return r;
    }).catch(() => caches.match(e.request)));
    return;
  }
  e.respondWith(caches.match(e.request).then(c => c || fetch(e.request)));
});