(function () {

    requirejs.config({
        paths: {
            'jquery': 'bower_components/jquery/dist/jquery'
        },
        packages: [
            { name: 'rest', location: 'bower_components/rest', main: 'browser' },
            { name: 'when', location: 'bower_components/when', main: 'when' },
            { name: 'sockjs-client', location: 'bower_components/sockjs-client', main: 'dist/sockjs.js'},
            { name: 'stomp-websocket', location: 'bower_components/stomp-websocket', main: 'lib/stomp.js' } //TODO Use minified version
        ],
        deps: [ 'app/main' ]
    });

}());