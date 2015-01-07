define(function(require) {
	'use strict';

	var rest = require('rest');
	var defaultRequest = require('rest/interceptor/defaultRequest');
	var mime = require('rest/interceptor/mime');
	var hal = require('rest/mime/type/application/hal');
	var baseRegistry = require('rest/mime/registry');

	var uriListConverter = require('./api/uriListConverter');

	var registry = baseRegistry.child();

	registry.register('text/uri-list', uriListConverter);
	registry.register('application/hal+json', hal);

	return rest
		.wrap(mime, { registry: registry })
		.wrap(defaultRequest, { headers: { 'Accept': 'application/hal+json' }});

});