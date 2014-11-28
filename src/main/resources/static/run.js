(function () {

    requirejs.config({
        paths: {
            'jquery': 'bower_components/jquery/dist/jquery'
        },
        packages: [
            { name: 'when', location: 'bower_components/when', main: 'when' },
            { name: 'rest', location: 'bower_components/rest', main: 'browser' }
        ],
        deps: [ 'app/main' ]
    });

}());