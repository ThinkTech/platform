const assets = ["/",".","js/dust-full.min.js"];
const cacheName = "assets-v-1-0";
self.addEventListener('install', function(event) {
  event.waitUntil(
    caches.open(cacheName).then(function(cache) {
      return cache.addAll(assets);
    })
  );
});
self.addEventListener('fetch', function(event) {
	  const request = event.request;
	  event.respondWith(
	    caches.open(cacheName).then(function(cache) {
	      return cache.match(request).then(function (response) {
	        return response || fetch(request).then(function(response) {
	          if(request.url.indexOf("/images/")!=-1){
	        	 cache.put(request, response.clone());
	          }
	          return response;
	        });
	      });
	    })
	  );
});