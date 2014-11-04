(function () {

requirejs.config({
	paths: {
		'jquery': 'jquery/dist/jquery'
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
	deps: [ 'app/main' ]
});

}());
