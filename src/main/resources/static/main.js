(function(define) { 'use strict';
    define(['jquery'], function($) {

        $(function() {
            console.log('main.js is loaded!');
        })

        return function() {

        }
    });
}(typeof define === 'function' && define.amd
    //assume metadata was requested by AMD
    ? define
    // otherwise, metadata is added to global springagram namespace
    : function() { if (!window.springagram) window.springagram = {}; window.springagram = foo(); }
));