"use strict";
// Service worker: cache-first for the big game package, network-first for the shell.
var CACHE = "eagler-fast-v1";
var SHELL = ["index.html", "bootstrap.js", "favicon.png"];

self.addEventListener("install", function(e) {
	self.skipWaiting();
	e.waitUntil(caches.open(CACHE).then(function(c) {
		return c.addAll(SHELL).catch(function() {});
	}));
});

self.addEventListener("activate", function(e) {
	e.waitUntil(caches.keys().then(function(keys) {
		return Promise.all(keys.filter(function(k) { return k !== CACHE; }).map(function(k) { return caches.delete(k); }));
	}).then(function() { return self.clients.claim(); }));
});

self.addEventListener("fetch", function(e) {
	var url = new URL(e.request.url);
	if (url.origin !== location.origin || e.request.method !== "GET") return;

	if (url.pathname.indexOf(".epw") !== -1) {
		// Cache-first: the 36 MB game package downloads once, then loads instantly forever
		e.respondWith(caches.open(CACHE).then(function(c) {
			return c.match(e.request).then(function(hit) {
				if (hit) return hit;
				return fetch(e.request).then(function(res) {
					if (res.ok) c.put(e.request, res.clone());
					return res;
				});
			});
		}));
		return;
	}

	// Shell: network-first with cache fallback (so updates roll out, but offline still works)
	e.respondWith(caches.open(CACHE).then(function(c) {
		return fetch(e.request).then(function(res) {
			if (res.ok) c.put(e.request, res.clone());
			return res;
		}).catch(function() {
			return c.match(e.request, { ignoreSearch: true }).then(function(hit) {
				return hit || c.match("index.html");
			});
		});
	}));
});
