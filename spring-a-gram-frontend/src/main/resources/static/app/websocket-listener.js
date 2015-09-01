define(function(require) {
	'use strict';

	var sockjs = require('sockjs-client');
	var stomp = require('stomp-websocket');

	return {
		register: register
	};

	function register(registrations) {
		var socket = new SockJS('/springagram');
		var stompClient = Stomp.over(socket);
		stompClient.connect({}, function(frame) {
			console.log('Connected: ' + frame);
			registrations.forEach(function (registration) {
				stompClient.subscribe(registration.route, registration.callback);
			});
		});
	}

});