self.addEventListener('install', event => {
    // Instalación rápida para PWA
    self.skipWaiting();
});

self.addEventListener('activate', event => {
    event.waitUntil(clients.claim());
});

self.addEventListener('fetch', event => {
    // Red passthrough básico - en un futuro se puede agregar cache offline
    event.respondWith(fetch(event.request));
});
