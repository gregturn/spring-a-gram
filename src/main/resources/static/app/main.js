define(function(require) {
	'use strict';

	var $ = require('jquery');
	var when = require('when');
	var api = require('./api');
	var follow = require('./follow');
	var twitter = require('./twitter');
	var imageReader = require('./imageReader');
	var imageShrinker = require('./imageShrinker');
	var hateoasHelper = require('./hateoasHelper');
	var stompClient = require('./websocket-listener.js');

	var currentGallery;
	var items = {};
	var galleries = {};
	var root = '/api';

	var talk = "https://devnexus.com/s/presentations#id-4417 on 3/11 @ 2:30pm";
	var tags = ['devnexus', 'REST'];

	var emptyImageTable = '<ul class="layout"></ul>';

	/* Search for a given item in the table of unlinked images */
	function findUnlinkedItem(item) {
		return $('#images li[data-uri="' + item._links.self.href + '"]');
	}

	/* Search for a given item in the gallery's table */
	function findLinkedItem(item) {
		return $('#gallery li[data-uri="' + item._links.self.href + '"]');
	}

	/* Delete the picture from storage and remove from the screen */
	function deletePic(item) {
		api({ method: 'DELETE', path: item._links.self.href })
			.done(function(response) {
			}, function(response) {
				if (response.status.code === 403) {
					alert('You are not authorized to delete that picture');
				}
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
		}).done(function() {
			$('#gallery ul li[data-uri="' + currentGallery._links.self.href +'"] ul').append(createItemRowForGallery(item, currentGallery));
			findUnlinkedItem(item).remove();
		}, function(response) {
			if (response.status.code === 403) {
				alert('You are not authorized to assign that picture to a gallery');
			}
		});
	}

	/* Unlink an item from it's gallery, and move it to the table of unlinked items */
	function removePicByResource(item, gallery) {
		if (gallery === undefined || item === undefined) {
			return;
		}

		item.clientFor('gallery')({method: 'DELETE'})
			.done(function(response) {
				if (response.status.code >= 200 && response.status.code < 300) {
					findLinkedItem(item).remove();
					$('#images ul').append(createItemRow(item));
				}
			});
	}

	/* Create a new table row for a item based on its gallery */
	function createItemRowForGallery(item, gallery) {
		var row = $('<li class="layout__item lap-and-up-1/2 desk-1/3"></li>')
			.attr('data-uri', item._links.self.href);

		var media = $('<div class="media media--responsive box box--tiny"></div>');

		media.append(
			$('<img>').addClass("media__img palm-1/1 lap-and-up-1/2").attr('src', item.image)
		);

		var mediaBody = $('<div class="media__body"></div>');

		var buttonLayout = $('<div class="layout"></div>');

		buttonLayout.append($('<a class="btn--responsive layout__item">Tweet</a>')
			.attr('href', twitter.tweetIntent(item, talk, tags))
			.attr('target', '_blank')
			.attr('data-uri', item._links.self.href));
		buttonLayout.append($('<button class="btn--responsive remove layout__item">Remove</button>')
			.attr('data-gallery-uri', gallery._links.self.href)
			.attr('data-uri', item._links.self.href));

		var permalink = $('<a></a>').attr('href', item.htmlUrl.href);
		permalink.append($('<button class="btn--responsive permalink layout__item">Permalink</button></a>'));
		buttonLayout.append(permalink);

		buttonLayout.append($('<span class="layout__item">' + item.user.name + '</span>'));

		mediaBody.append(buttonLayout);

		media.append(mediaBody);

		row.append(media);

		return row;
	}

	/* Draw the gallery table from scratch */
	function drawGalleryTable(data) {
		var table = $('<ul class="layout"></ul>');

		return when.map(data, function (gallery) {
			var row = $('<li class="layout__item"></li>').attr('data-uri', gallery._links.self.href);

			row.append($('<input type="radio" name="gallery" class="btn--responsive">').click(function () {
					currentGallery = gallery;
			}));

			row.append($('<span></span>').text(gallery.description));

			var nestedTable = $('<ul class="layout"></ul>');
			row.append(nestedTable);
			table.append(row);
			$('#gallery').append(table);

			/* Now that the table is configured, start adding items to the nested table */
			var galleryItems = getGalleryItems(gallery);
			return when.map(galleryItems, function(itemWithImage) {
				items[itemWithImage.entity._links.self.href] = itemWithImage.entity;
				nestedTable.append(createItemRowForGallery(itemWithImage.entity, gallery));
			});
		});
	}

	function getGalleryItems (gallery) {
		return api({
			method: 'GET',
			path: gallery._links.items.href,
			params: { projection: "noImages" }
		}).then(function (response) {
			return getEmbeddedItems(response.entity);
		});
	}

	function getEmbeddedItems(galleryItems) {
		var embedded = galleryItems._embedded;
		if (!embedded) {
			return [];
		}

		return embedded.items.map(function (itemWithoutImage) {
			return api({
				path: itemWithoutImage._links.self.href,
				params: { projection: "owner" }
			});
		});
	}

	/* Create a new table row for an unlinked item */
	function createItemRow(item) {
		var row = $('<li class="layout__item lap-and-up-1/2 desk-1/3"></li>').attr('data-uri', item._links.self.href);

		var media = $('<div class="media media--responsive box box--tiny"></div>');

		media.append(
			$('<img>').addClass("media__img palm-1/1 lap-and-up-1/2").attr('src', item.image)
		);

		var mediaBody = $('<div class="media__body"></div>');

		var buttonLayout = $('<div class="layout"></div>');

		buttonLayout.append($('<a class="btn--responsive layout__item">Tweet</a>')
			.attr('href', twitter.tweetIntent(item, talk, tags))
			.attr('target', '_blank')
			.attr('data-uri', item._links.self.href));
		buttonLayout.append($('<button class="btn--responsive add-to-gallery layout__item">Add To Gallery</button>')
			.attr('data-uri', item._links.self.href));
		buttonLayout.append($('<button class="btn--responsive delete layout__item">Delete</button>')
			.attr('data-uri', item._links.self.href));

		var permalink = $('<a></a>').attr('href', item.htmlUrl.href);
		permalink.append($('<button class="btn--responsive permalink layout__item">Permalink</button></a>'));
		buttonLayout.append(permalink);

		buttonLayout.append($('<span class="layout__item">Uploaded by: ' + item.user.name + '</span>'));

		mediaBody.append(buttonLayout);

		media.append(mediaBody);

		row.append(media);

		return row;
	}

	/* Append an item's table row to the image table */
	function addItemRow(item) {
		$('#images ul').append(createItemRow(item));
	}

	/* When the page is loaded, run/register this set of code */
	$(function() {
		var imagesEl = $('#images');

		/* When upload is clicked, upload the file, store it, and then add to list of unlinked items */
		$('#upload').submit(function (e) {
			e.preventDefault();

			var fileInput = $('#file')[0];

			if(!imageReader.hasImage(fileInput)) {
				return;
			}

			imageReader.readImage(fileInput).then(function(image) {
				console.log("Original image is " + image.length + " bytes long");
				return imageShrinker.shrink(image, 100, 75);
			}).done(function(image) {
				console.log("Image has been shrunk to " + image.length + " bytes");
				api({
					method: 'POST',
					path: root + '/items',
					entity: {image: image},
					headers: {'Content-Type': 'application/json'}
				});
			});
		});

        $('#newGallery').submit(function (e) {
            e.preventDefault();

            console.log(e);
            console.log($('#description')[0].value);
        });

		/* Listen for clicks on the gallery */
		$('#gallery').on('click', '.remove', function(e) {
			var itemUri = e.target.dataset['uri'];
			var galleryUri = e.target.dataset['galleryUri'];
			removePicByResource(items[itemUri], galleries[galleryUri]);
		});

		/* Listen for clicks on the list of images */
		imagesEl.on('click', '.delete', function(e) {
			var itemUri = e.target.dataset['uri'];
			deletePic(items[itemUri]);
		});

		imagesEl.on('click', '.add-to-gallery', function(e) {
			var itemUri = e.target.dataset['uri'];
			addToSelectedGallery(items[itemUri]);
		});

		// If this is a single image page, then configure href for the anchor
		var onePage = document.querySelector('#tweetOneImage');
		if (onePage) {
			onePage.href = twitter.tweetIntent(hateoasHelper.wrapHref(window.location.href), talk, tags);
		}

		var galleriesReady = follow(api, root, ['galleries', 'galleries'])
			.then(function(response) {
				response.forEach(function(gallery) {
					galleries[gallery._links.self.href] = gallery;
				});

				return drawGalleryTable(response);
			});

		// tag::find-unlinked-images[]
		var itemsReady = follow(api, root, [
			{ rel: 'items', params: { projection: "noImages"} },
			'search',
			{ rel: 'findByGalleryIsNull', params: { projection: "noImages" } },
			'items'])
			.then(function(response) {

				imagesEl.append(emptyImageTable);

				return when.map(response, function(itemWithoutImage) {
					return api({
						path: itemWithoutImage._links.self.href,
						params: { projection: "owner" }
					}).then(function(item) {
						items[item.entity._links.self.href] = item.entity;
						addItemRow(item.entity);
					});
				});
			});
		// end::find-unlinked-images[]

		stompClient.register([
			{ route: '/topic/newItem', callback: showNewItem },
			{ route: '/topic/deleteItem', callback: deleteItem },
			{ route: '/topic/removeItemFromGallery', callback: removeItemFromGallery },
			{ route: '/topic/addItemToGallery', callback: addItemToGallery }
		]);

		function showNewItem(message) {
			var href = message.body;
			api({
				method: 'GET',
				path: href,
				params: { projection: "owner" }
			}).done(function(response) {
				var item = response.entity;
				console.log(item);
				console.log('Adding ' + item._links.self.href + ' to Items');
				items[item._links.self.href] = item;
				addItemRow(item);
			});
		}

		function deleteItem(message) {
			var href = message.body
			for (var property in items) {
				if (items.hasOwnProperty(property)) {
					// Add '{' to href to avoid 'http://localhost:8080/api/items/50{?projection}' accidentally matching
					// '/api/items/5'
					if (~property.indexOf(href + '{')) {
						console.log(href + " matches " + property + " so I'm deleting it off the page");
						findUnlinkedItem(hateoasHelper.wrapSelfHref(property)).remove();
						delete items[property];
					}
				}
			}
		}

		function removeItemFromGallery(message) {
			var href = message.body;
			console.log(href + ' just had a link removed!');
		}

		function addItemToGallery(message) {
			var href = message.body;
			console.log(href + ' just had a link saved!');
		}

	});
});
