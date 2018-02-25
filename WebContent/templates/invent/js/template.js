$(document).ready(function(){
	if('serviceWorker' in navigator) {
		navigator.serviceWorker.register('sw.js');
	};
});