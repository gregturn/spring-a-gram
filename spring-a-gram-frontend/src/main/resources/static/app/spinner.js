define(function (require) {
	'use strict';

	return {
		showSpinner: function () {
			document.getElementById('spinner').style.cssText = '';
		},

		hideSpinner: function () {
			document.getElementById('spinner').style.cssText = 'display: none;';
		}
	}

});