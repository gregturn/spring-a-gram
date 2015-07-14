define(function(require) {
	'use strict';

	document.addEventListener('DOMContentLoaded', new function() {

		require('jsx!app/datagrid');
		require('jsx!app/upload');

	}, false);

});
