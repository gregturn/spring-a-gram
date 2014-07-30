(function () {

requirejs.config({
	paths: {
		'jquery': 'jquery/dist/jquery',
		'jquery-mobile': 'jquery-mobile-bower/js/jquery.mobile-1.4.2'
	},
	packages: [
		{ name: 'when', location: 'when', main: 'when' },
		{ name: 'rest', location: 'rest', main: 'rest' }
	],
	map: {
		'*': {
			'css': 'require-css/css'
		}
	},
	shim: {
		'jquery-mobile': {
			deps: [ 'jquery' ],
			exports: '$'
		}
	},
	deps: isMobile() ? [ 'mobile' ] : [ 'app/main' ]
});

function isMobile () {
	var html = document.documentElement;
	return html.getAttribute('data-device') === 'mobile';
}

}());
