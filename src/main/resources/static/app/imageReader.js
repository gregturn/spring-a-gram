define(function(require) {

	var when = require('when');

	return {
		hasImage: hasImage,
		readImage: readImage
	};

	function hasImage(input) {
		return input.files
			&& input.files[0]
			&& input.files[0].type.indexOf('image') != -1
	}

	function readImage(input) {
		return when.promise(function(resolve, reject) {
			var fileReader = new FileReader();

			fileReader.onerror = reject;
			fileReader.onloadend = function () {
				resolve({ name: input.files[0].name, image: fileReader.result });
			};

			fileReader.readAsDataURL(input.files[0]);
		});
	}
});