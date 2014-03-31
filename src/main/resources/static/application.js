(function() {

    var name, bytes, currentGallery;
    var repositories = sdr.createRepositories();
    var itemRepository = repositories.itemRepository;
    var galleryRepository = repositories.galleryRepository;
    var items = {};
    var galleries = {};

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
        itemRepository.delete(id(item)).done(function () {
            findUnlinkedItem(item).remove();
            delete items[item._links.self.href];
        });
    }

    /* Move the picture from table of unlinked items to its gallery's table */
    function addToSelectedGallery(item) {
        var addItems = galleryRepository.addItems(currentGallery, item);
        var setGallery = itemRepository.setGallery(item, currentGallery);

        $.when(addItems, setGallery).then(function () {
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
        var removeItems = galleryRepository.removeItems(gallery, id(item));
        var deleteGallery = itemRepository.deleteGallery(item);

        $.when(removeItems, deleteGallery).then(function () {
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
        if (data._embedded) {
            data._embedded.galleries.forEach(function (gallery) {
                var row = $('<tr></tr>').attr('data-uri', gallery._links.self.href);

                row.append($('<td></td>').append(
                    $('<input type="radio" name="gallery">').click(function () {
                        currentGallery = gallery;
                    })
                ));

                row.append($('<td></td>').text(gallery.description));

                var nestedTable = $('<table></table>');
                nestedTable.append('<tr><th>Filename</th><th>Image</th></tr>');
                galleryRepository.getItems(gallery).done(function (data) {
                    if (data._embedded) {
                        data._embedded.items.forEach(function (item) {
                            nestedTable.append(createItemRowForGallery(item, gallery));
                        });
                    }
                });

                row.append($('<td></td>').append(nestedTable));

                table.append(row);
            });
        }
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

    /* When the page is loaded, run/register this set of code */
    $(function () {

        /* Listen for picking a file */
        $('#file').change(function () {
            readImage(this);
        });

        /* When upload is clicked, upload the file, store it, and then add to list of unlinked items */
        $('#upload').submit(function (e) {
            e.preventDefault();
            var response = itemRepository.create({
                name: name,
                image: bytes
            });
            response.done(function () {
                itemRepository.findOne(id(response.getResponseHeader("Location"))).done(function(item) {
                    items[item._links.self.href] = item;
                    addItemRow(item);
                });
            })
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

        galleryRepository.findAll().done(function(data) {
            if (data._embedded) {
                data._embedded.galleries.forEach(function(gallery) {
                    galleries[gallery._links.self.href] = gallery;
                });
            }
            drawGalleryTable(data);
        });

        itemRepository.findByGalleryIsNull().done(function(data) {
            var table = $('<table></table>');
            table.append('<tr><th>Filename</th><th>Image</th><th></th><th></th></tr>');
            $('#images').append(table);
            if (data._embedded) {
                data._embedded.items.forEach(function(item) {
                    items[item._links.self.href] = item;
                    addItemRow(item);
                })
            }
        });

    });

})();