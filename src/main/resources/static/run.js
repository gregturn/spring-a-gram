(function () {

    requirejs.config({
        paths: {
            'jquery': 'bower_components/jquery/dist/jquery'
        },
        packages: [
            { name: 'rest', location: 'bower_components/rest', main: 'browser' },
            { name: 'when', location: 'bower_components/when', main: 'when' }
        ],
        deps: [ 'app/main' ]
    });

}());