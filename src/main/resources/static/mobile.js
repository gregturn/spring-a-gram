(function(define) { 'use strict';
    define(function(require) {

        require('css!jquery-mobile-bower/css/jquery.mobile-1.4.2.min.css');
        require('jquery-mobile');

        var $ = require('jquery');
        var rest = require('rest');
        var when = require('when');
        var defaultRequest = require('rest/interceptor/defaultRequest');
        var mime = require('rest/interceptor/mime');
        var hateoas = require('rest/interceptor/hateoas');
        var hal = require('rest/mime/type/application/hal');
        var registry = require('rest/mime/registry');
        var interceptor = require('rest/interceptor');

        var uriTemplateInterceptor = interceptor({
            request: function (request, config, meta) {
                /* If the URI is a URI Template per RFC 6570 (http://tools.ietf.org/html/rfc6570), trim out the template part */
                if (request.path.indexOf('{') === -1) {
                    return request;
                } else {
                    request.path = request.path.split('{')[0];
                    return request;
                }
            }
        })

        var name, bytes, currentGallery;
        var api = rest
            .wrap(mime)
            .wrap(hateoas)
            .wrap(defaultRequest, {headers: {'Accept': 'application/hal+json'}})
            .wrap(uriTemplateInterceptor);
        var items = {};
        var galleries = {};
        var currentItem;
        var root = '/api';

        /* Convert a single or array of resources into "URI1\nURI2\nURI3..." */
        var uriListConverter = {
            read: function(str, opts) {
                return str.split('\n');
            },
            write: function(obj, opts) {
                // If this is an Array, extract the self URI and then join using a newline
                if (obj instanceof Array) {
                    return obj.map(function(resource) {
                        return resource._links.self.href;
                    }).join('\n');
                } else { // otherwise, just return the self URI
                    return obj._links.self.href;
                }
            }
        };
        registry.register('text/uri-list', uriListConverter);
        registry.register('application/hal+json', hal);

        function readAndUploadImage(input) {
            if (input.files && input.files[0]) {
                if (input.files[0].type.indexOf('image') != -1) {
                    var FR = new FileReader();
                    FR.onloadend = function () {
                        name = input.files[0].name;
                        bytes = FR.result;
                        $.mobile.loading('show', {
                            text: 'Uploading ' + name,
                            textVisible: true
                        });
                        api({
                            method: 'POST',
                            path: root + '/items',
                            entity: {
                                name: name,
                                image: bytes
                            },
                            headers: {'Content-Type': 'application/json'}
                        }).done(function(response) {
                            api({
                                method: 'GET',
                                path: response.headers.Location
                            }).done(function(response) {
                                var item = response.entity;
                                items[item._links.self.href] = item;
                                when($('#piclist').append(addItemRow(item))).done(function() {
                                    $('#piclist').listview('refresh');
                                    $.mobile.loading('hide');
                                });
                            });
                        });
                    }
                    FR.readAsDataURL(input.files[0]);
                }
            }
        }

        /* Take either a JSON or URI version of a resource, and extract it's ID */
        function id(resource) {
            if (typeof resource === "string") {
                var parts = resource.split("/");
            } else {
                var parts = resource._links.self.href.split("/");
            }
            return parts[parts.length - 1];
        }

        function follow(root, relArray) {
            var root = api({
                method: 'GET',
                path: root
            });
            relArray.forEach(function(arrayItem) {
                var rel;
                if (typeof arrayItem === 'string') {
                    var rel = arrayItem
                } else {
                    var rel = arrayItem.rel;

                }

                root = root.then(function (response) {
                    if (response.entity._embedded && response.entity._embedded.hasOwnProperty(rel)) {
                        return response.entity._embedded[rel];
                    } else {
                        if (typeof arrayItem === 'string') {
                            return api({
                                method: 'GET',
                                path: response.entity._links[rel].href
                            });
                        } else {
                            return api({
                                method: 'GET',
                                path: response.entity._links[rel].href,
                                params: arrayItem.params
                            });
                        }
                    }
                });
            });
            return root;
        }

        function createGalleryItemRow(item) {
            var list = $('<li></li>').attr('data-uri', item._links.self.href);

            list.append(
                $('<a data-rel="popup" data-transition="fade" data-position-to="window"></a>')
                    .attr('href', '#view-gallery-pic')
                    .append($('<img />').attr('src', item.image))
            );

            list.append(
                $('<a href="#galleryOps" data-rel="popup" data-transition="flow" data-icon="gear"></a>').append(
                    $('<img />').attr('src', item.image)
                )
            );

            return list;
        }

        function addGalleryRow(gallery) {
            return api({
                method: 'GET',
                path: gallery._links.items.href
            }).then(function(response) {
                var collapsible = $('<div data-role="collapsible" data-mini="true" data-collapsed-icon="carat-d" data-expanded-icon="carat-u"></div>');

                collapsible.append(
                    $('<h4></h4>').text(gallery.description)
                );

                // Start with an empty collection
                var collection = $('<ul data-role="listview"></ul>').attr('data-gallery-uri', gallery._links.self.href);

                // If there are any items, add them
                if (response.entity._embedded) {
                    collection = response.entity._embedded.items.reduce(
                        function(combined, item) {
                            items[item._links.self.href] = item;
                            return combined.append(createGalleryItemRow(item));
                        },
                        collection
                    );
                }
                collection.listview().enhanceWithin();
                collapsible.append(collection);
                collapsible.collapsible().enhanceWithin();

                return collapsible;
            });
        }

        /* When the page is loaded, run/register this set of code */
        function addItemRow(item) {
            var list = $('<li></li>').attr('data-uri', item._links.self.href);

            list.append(
                $('<a data-rel="popup" data-transition="fade" data-position-to="window"></a>')
                    .attr('href', '#view')
                    .append($('<img />').attr('src', item.image))
            );

            list.append($('<a href="#pic_ops" data-rel="popup" data-transition="flow" data-icon="gear"></a>'));

            return list;
        }

        function addGalleryOption(gallery) {
            var radioButton = $('<input type="radio" name="gallery" />').attr("id", gallery._links.self.href);
            var label = $('<label />')
                .attr('for', gallery._links.self.href)
                .text(gallery.description);
            radioButton.enhanceWithin();
            label.enhanceWithin();
            var fieldset = $('#addToGallery fieldset');
            fieldset.append(radioButton).append(label);
            //fieldset.controlgroup('refresh').enhanceWithin();
        }

        $(function() {

            /* Listen for picking a file */
            $('input[type="file"]').on('change', function () {
                readAndUploadImage(this);
            });

            $('#gallerylist').on('click', function(e) {
                if (e.target.tagName === 'IMG') {
                    currentItem = items[e.target.parentNode.parentNode.dataset['uri']];
                    $('#remove').attr('data-uri', currentItem);
                }
                if (e.target.tagName === 'A') {
                    currentItem = items[e.target.parentNode.dataset['uri']];
                    $('#remove').attr('data-uri', currentItem);
                }
                $('#view-gallery-pic img').attr('src', currentItem.image);
            });

            $('#remove').on('click', function(e) {
                api({ method: 'DELETE', path: currentItem._links.gallery.href }).done(function(response) {
                    $('#gallerylist li[data-uri="' + currentItem._links.self.href + '"]').remove();
                    when($('#piclist').append(addItemRow(currentItem))).done(function() {
                        $('#piclist').listview('refresh');
                    });
                });
            })

            $('#piclist').on('click', function(e) {
                if (e.target.tagName === 'IMG') {
                    currentItem = items[e.target.parentNode.parentNode.dataset['uri']];
                }
                if (e.target.tagName === 'A') {
                    currentItem = items[e.target.parentNode.dataset['uri']];
                }
                $('#view img').attr('src', currentItem.image);
            });

            $('#deleteConfirmed').on('click', function(e) {
                api({
                    method: 'DELETE',
                    path: currentItem._links.self.href
                }).done(function() {
                    $('#piclist li[data-uri="' + currentItem._links.self.href + '"]').remove();
                    delete items[currentItem._links.self.href];
                }).done(function() {
                    $('#piclist').listview('refresh');
                    currentItem = undefined;
                    $('body').pagecontainer('change', '#pictures');
                });
            });

            $('#addToGallery').on('change', function(e) {
                currentGallery = galleries[e.target.id];
            });

            $('#addToGallery').on('click', function(e) {
                if (e.target.tagName === 'INPUT') {
                    if (currentGallery === undefined) {
                        return;
                    }

                    api({
                        method: 'PUT',
                        path: currentItem._links.gallery.href,
                        entity: currentGallery,
                        headers: {'Content-Type': 'text/uri-list'}
                    }).done(function() {
                        var collection = $('#gallerylist ul[data-gallery-uri="' + currentGallery._links.self.href + '"]');
                        when(
                            collection.append(createGalleryItemRow(currentItem)),
                            $('#piclist li[data-uri="' + currentItem._links.self.href + '"]').remove()
                        ).done(function() {
                            collection.listview('refresh');
                            $('#piclist').listview('refresh');
                            $('body').pagecontainer('change', '#home');
                        });
                    })
                }
            });

            follow(root, ['galleries', 'galleries']).then(function(response) {
                var gallerylist = $('#gallerylist');
                response.forEach(function(gallery) {
                    galleries[gallery._links.self.href] = gallery;
                    addGalleryRow(gallery).done(function(response) {
                        gallerylist.append(response);
                    })
                    addGalleryOption(gallery);
                });
            });

            follow(root, [
                { rel: 'items', params: { projection: "noImages"} },
                'search',
                { rel: 'findByGalleryIsNull', params: { projection: "noImages" } },
                'items']).done(function(response) {
                var piclist = $('#piclist');
                response.forEach(function(itemWithoutImage) {
                    api({path: itemWithoutImage._links. self.href}).done(function(item) {
                        items[item.entity._links.self.href] = item.entity;
                        piclist.append(addItemRow(item.entity));
                        piclist.listview('refresh');
                    });
                });
            });
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