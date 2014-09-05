(function(document) {
define(function(require) {
	'use strict';

	return {
		tweetIntent: tweetIntent
	};

	function tweetIntent(item, talk, tags) {
		return "https://twitter.com/intent/tweet?text=" +
			encodeURIComponent(item.htmlUrl.href + " was uploaded by Spring-a-Gram. See how at " + talk) +
			"&hashtags=" + tags.join(',');
	}

});
}(document));
