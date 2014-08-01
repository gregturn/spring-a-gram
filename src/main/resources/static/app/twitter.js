(function(document) {
define(function(require) {
	'use strict';

	var $ = require('jquery');

	return {
		tweetButton: tweetButton,
		tweetPic: tweetPic
	};

	function tweetButton(href) {
		return $('<a>Tweet me!</a>')
			.attr('href', 'https://twitter.com/share')
			.addClass('twitter-share-button')
			.attr('data-text', href + ' was uploaded by Spring-a-Gram.')
			.attr('data-url', 'https://2014.event.springone2gx.com/schedule/sczbpf')
			.attr('data-hashtags', 's2gx,rest,hypermedia')
			.attr('data-related', 'springcentral,springone2gx')
			.attr('target', '_blank');
	}

	function tweetPic() {
		!function(d,s,id) {
			var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';
			if(!d.getElementById(id)){
				js = d.createElement(s);
				js.id = id;
				js.src = p+'://platform.twitter.com/widgets.js';
				fjs.parentNode.insertBefore(js,fjs);
			}
		}(document, 'script', 'twitter-wjs');
	}

});
}(document));
