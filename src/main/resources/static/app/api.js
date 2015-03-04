define(function(require) {
	'use strict';

	var rest = require('rest');
	var defaultRequest = require('rest/interceptor/defaultRequest');
	var mime = require('rest/interceptor/mime');
	var baseRegistry = require('rest/mime/registry');

	var registry = baseRegistry.child();

	var uriTemplateInterceptor = require('./api/uriTemplateInterceptor');
	registry.register('text/uri-list', require('./api/uriListConverter'));
	registry.register('application/hal+json', require('rest/mime/type/application/hal'));

	return rest
		.wrap(mime, { registry: registry })
		.wrap(uriTemplateInterceptor)
		.wrap(defaultRequest, { headers: { 'Accept': 'application/hal+json' }});

});