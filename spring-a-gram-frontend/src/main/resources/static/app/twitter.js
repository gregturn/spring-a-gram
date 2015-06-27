(function(document) {
define(function(require) {
	'use strict';

	var linkHelper = require('./linkHelper');

	return {
		tweetIntentWithHref: tweetIntentWithHref,
		tweetIntent: tweetIntent
	};

	function tweetIntentWithHref(href, talk, tags) {
		return "https://twitter.com/intent/tweet?text=" +
			encodeURIComponent(href + " was uploaded by Spring-a-Gram. ") +
			"&hashtags=" + tags.join(',');
	}

	function tweetIntent(item, talk, tags) {
		return tweetIntentWithHref(linkHelper.htmlUrl(item._links.self.href), talk, tags);
	}

});
}(document));
