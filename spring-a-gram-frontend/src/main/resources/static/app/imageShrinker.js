define(function(require) {
	'use strict';

	var when = require('when');

	return {
		shrink: shrink
	};

	/**
	 * Return a canvas object with a shrunken image
	 */
	function shrink(image, maxWidth, maxHeight) {
		return when.promise(function(resolve, reject) {
			var canvas = document.createElement('canvas');
			var img = new Image();
			img.onload = function() {
				var MAX_WIDTH = maxWidth;
				var MAX_HEIGHT = maxHeight;
				var width = img.width;
				var height = img.height;

				if (width > height) {
					if (width > MAX_WIDTH) {
						height *= MAX_WIDTH / width;
						width = MAX_WIDTH;
					}
				} else {
					if (height > MAX_HEIGHT) {
						width *= MAX_HEIGHT / height;
						height = MAX_HEIGHT;
					}
				}
				canvas.width = width;
				canvas.height = height;

				var ctx = canvas.getContext('2d');
				ctx.drawImage(img, 0, 0, width, height);
				resolve(canvas.toDataURL('image/png'));
			}
			img.src = image;
		});
	};

});