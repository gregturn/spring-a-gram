(function(document) {
	define(function(require) {
		'use strict';

		return {
			htmlUrl: htmlUrl
		};

		function htmlUrl(uri) {
			var encodedUri = encodeURIComponent(uri.split('{')[0]);
			var link = window.location.origin + '/image?link=' + encodedUri;
			return link;
		}

	});
}(document));
