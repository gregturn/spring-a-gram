(function () {

    requirejs.config({
        paths: {
            'jquery': 'node_modules/jquery/dist/jquery'
        },
        packages: [
            { name: 'rest', location: 'node_modules/rest', main: 'browser' },
            { name: 'when', location: 'node_modules/rest/node_modules/when', main: 'when' }
        ],
        deps: [ 'app/main' ]
    });

}());