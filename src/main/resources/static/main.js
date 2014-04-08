(function(define) { 'use strict';
    define(function(require) {

        var $ = require('jquery');
        var rest = require('rest');
        var when = require('when');
        var mime = require('rest/interceptor/mime');
        var hateoas = require('rest/interceptor/hateoas');
        var hal = require('rest/mime/type/application/hal');
        var registry = require('rest/mime/registry');

        var name, bytes, currentGallery;
        var api = rest.chain(mime).chain(hateoas);
        var items = {};
        var galleries = {};

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

        function readImage(input) {
            if (input.files && input.files[0]) {
                if (input.files[0].type.indexOf('image') != -1) {
                    var FR = new FileReader();
                    FR.onloadend = function () {
                        name = input.files[0].name;
                        bytes = FR.result;
                    }
                    FR.readAsDataURL(input.files[0]);
                }
            }
        }

        /* Search for a given item in the table of unlinked images */
        function findUnlinkedItem(item) {
            return $('#images tr[data-uri="' + item._links.self.href + '"]');
        }

        /* Search for a given item in the gallery's table */
        function findLinkedItem(item) {
            return $('#gallery tr[data-uri="' + item._links.self.href + '"]');
        }

        /* Delete the picture from storage and remove from the screen */
        function deletePic(item) {
            api({ method: 'DELETE', path: item._links.self.href }).then(function(response) {
                findUnlinkedItem(item).remove();
                delete items[item._links.self.href];
            });
        }

        /* Move the picture from table of unlinked items to its gallery's table */
        function addToSelectedGallery(item) {
            if (currentGallery === undefined) {
                return;
            }

            api({
                method: 'PUT',
                path: item._links.gallery.href,
                entity: currentGallery,
                headers: {'Content-Type': 'text/uri-list'}
            }).then(function() {
                $('#gallery table table').append(createItemRowForGallery(item, currentGallery));
                findUnlinkedItem(item).remove();
            });
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

        /* Unlink an item from it's gallery, and move it to the table of unlinked items */
        function removePicByResource(item, gallery) {
            if (gallery === undefined || item === undefined) {
                return;
            }

            api({
                method: 'DELETE',
                path: item._links.gallery.href
            }).then(function(response) {
                findLinkedItem(item).remove();
                $('#images table').append(createItemRow(item));
            });
        }

        /* Create a new table row for a item based on its gallery */
        function createItemRowForGallery(item, gallery) {
            var row = $('<tr></tr>')
                .attr('data-uri', item._links.self.href);

            row.append($('<td></td>').text(item.name));

            row.append($('<td></td>').append(
                $('<img>').addClass('thumbnail').attr('src', item.image)
            ));

            row.append($('<td></td>').append(
                $('<button>Remove</button>')
                    .attr('data-gallery-uri', gallery._links.self.href)
                    .attr('data-uri', item._links.self.href)
            ));
            return row;
        }

        /* Draw the gallery table from scratch */
        function drawGalleryTable(data) {
            var table = $('<table></table>');
            table.append('<tr><th></th><th>Name</th><th>Collection</th></tr>')
            data.forEach(function (gallery) {
                console.log(gallery);
                var row = $('<tr></tr>').attr('data-uri', gallery._links.self.href);

                row.append($('<td></td>').append(
                    $('<input type="radio" name="gallery">').click(function () {
                        currentGallery = gallery;
                    })
                ));

                row.append($('<td></td>').text(gallery.description));

                var nestedTable = $('<table></table>');
                nestedTable.append('<tr><th>Filename</th><th>Image</th></tr>');
                api({
                    method: 'GET',
                    path: gallery._links.items.href
                }).then(function(response) {
                    if (response.entity._embedded) {
                        response.entity._embedded.items.forEach(function (item) {
                            items[item._links.self.href] = item;
                            nestedTable.append(createItemRowForGallery(item, gallery));
                        });
                    }
                });

                row.append($('<td></td>').append(nestedTable));

                table.append(row);
            });
            $('#gallery').append(table);
        }

        /* Create a new table row for an unlinked item */
        function createItemRow(item) {
            var row = $('<tr></tr>').attr('data-uri', item._links.self.href);

            row.append($('<td></td>').text(item.name));

            row.append($('<td></td>').append(
                $('<img>').addClass('thumbnail').attr('src', item.image)
            ));

            row.append($('<td></td>').append(
                $('<button>Delete</button>')
            ));

            row.append($('<td></td>').append(
                $('<button>Add To Gallery</button>')
            ));

            return row;
        }

        /* Append an item's table row to the image table */
        function addItemRow(item) {
            $('#images table').append(createItemRow(item));
        }

        function follow(relArray) {
            var root = api({
                method: 'GET',
                path: '/',
                headers: {'Accept': 'application/hal+json'}
            });
            relArray.forEach(function(rel) {
                root = root.then(function (response) {
                    if (response.entity._embedded && response.entity._embedded.hasOwnProperty(rel)) {
                        return response.entity[rel];
                    } else {
                        return response.entity.clientFor(rel)({headers: {'Accept': 'application/hal+json'}});
                    }
                });
            });
            return root;
        }

        /* When the page is loaded, run/register this set of code */
        $(function() {
            /* Listen for picking a file */
            $('#file').change(function () {
                readImage(this);
            });

            /* When upload is clicked, upload the file, store it, and then add to list of unlinked items */
            $('#upload').submit(function (e) {
                e.preventDefault();
                api({
                    method: 'POST',
                    path: '/items',
                    entity: {
                        name: name,
                        image: bytes
                    },
                    headers: {'Content-Type': 'application/json'}
                }).then(function(response) {
                    api({
                        method: 'GET',
                        path: response.headers.Location
                    }).then(function(response) {
                        var item = response.entity;
                        items[item._links.self.href] = item;
                        addItemRow(item);
                    });
                });
            });

            /* Listen for clicks on the gallery */
            $('#gallery').on('click', function(e) {
                if (e.target.localName === 'button') {
                    if (e.target.innerText === 'Remove') {
                        var itemUri = e.target.dataset['uri'];
                        var galleryUri = e.target.dataset['galleryUri'];
                        removePicByResource(items[itemUri], galleries[galleryUri]);
                    }
                }
            });

            /* Listen for clicks on the list of images */
            $('#images').on('click', function(e) {
                if (e.target.localName === 'button') {
                    if (e.target.innerText === 'Delete') {
                        var itemUri = e.target.parentNode.parentNode.dataset['uri'];
                        deletePic(items[itemUri]);
                    } else if (e.target.innerText === 'Add To Gallery') {
                        var itemUri = e.target.parentNode.parentNode.dataset['uri'];
                        addToSelectedGallery(items[itemUri]);
                    }
                }
            });

            follow(['galleries', 'galleries']).then(function(response) {
                response.forEach(function(gallery) {
                    galleries[gallery._links.self.href] = gallery;
                });
                drawGalleryTable(response);
            })

            follow(['items', 'search', 'findByGalleryIsNull', 'items']).then(function(response) {
                var table = $('<table></table>');
                table.append('<tr><th>Filename</th><th>Image</th><th></th><th></th></tr>');
                $('#images').append(table);
                response.forEach(function(item) {
                    items[item._links.self.href] = item;
                    addItemRow(item);
                })
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