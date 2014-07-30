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
	deps: isMobile() ? [ 'mobile' ] : [ 'main' ]
});

function isMobile () {
	return window.sagDeviceType === 'mobile';
}

}());
