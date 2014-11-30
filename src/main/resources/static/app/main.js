define(function(require) {
	'use strict';

	var $ = require('jquery');
	var when = require('when');
	var api = require('./api');
	var follow = require('./follow');
	var twitter = require('./twitter');
	var imageReader = require('./imageReader');
	var hateoasHelper = require('./hateoasHelper');

	var currentGallery;
	var items = {};
	var galleries = {};
	var root = '/api';

	var talk = "https://2014.event.springone2gx.com/schedule/sessions/spring_data_rest_data_meets_hypermedia.html";
	var tags = ['s2gx', 'REST'];

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
			.done(function() {
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
		}).done(function() {
			$('#gallery ul li[data-uri="' + currentGallery._links.self.href +'"] ul').append(createItemRowForGallery(item, currentGallery));
			findUnlinkedItem(item).remove();
		});
	}

	/* Unlink an item from it's gallery, and move it to the table of unlinked items */
	function removePicByResource(item, gallery) {
		if (gallery === undefined || item === undefined) {
			return;
		}

		api({
			method: 'DELETE',
			path: item._links.gallery.href
		}).done(function() {
			findLinkedItem(item).remove();
			$('#images ul').append(createItemRow(item));
		});
	}

	/* Create a new table row for a item based on its gallery */
	function createItemRowForGallery(item, gallery) {
		var row = $('<li></li>')
			.attr('data-uri', item._links.self.href);

		row.append($('<span></span>').append(
			$('<a></a>').attr('href', item.htmlUrl.href).append(
				$('<img>').addClass('thumbnail').attr('src', item.image)
			)
		));

		row.append($('<span></span>').append(
			$('<a>Tweet</a>').attr('href', twitter.tweetIntent(item, talk, tags))
				.attr('target', '_blank')
		));

		row.append($('<span></span>').append(
			$('<button class="remove">Remove</button>')
				.attr('data-gallery-uri', gallery._links.self.href)
				.attr('data-uri', item._links.self.href)
		));
		return row;
	}

	/* Draw the gallery table from scratch */
	function drawGalleryTable(data) {
		var table = $('<ul></ul>');

		return when.map(data, function (gallery) {
			var row = $('<li></li>').attr('data-uri', gallery._links.self.href);

			row.append($('<span></span>').append(
				$('<input type="radio" name="gallery">').click(function () {
					currentGallery = gallery;
				})
			));

			row.append($('<span></span>').text(gallery.description));

			var nestedTable = $('<ul></ul>');
			row.append($('<span></span>').append(nestedTable));
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
				path: itemWithoutImage._links.self.href
			});
		});
	}

	/* Create a new table row for an unlinked item */
	function createItemRow(item) {
		var row = $('<li class="layout__item lap-and-up-1/2 desk-1/5"></li>').attr('data-uri', item._links.self.href);

		row.append($('<span></span>').append(
			$('<a></a>').attr('href', item.htmlUrl.href).append(
				$('<img>').addClass('thumbnail').attr('src', item.image)
			)
		));

		row.append($('<span></span>').append(
			$('<a>Tweet</a>').attr('href', twitter.tweetIntent(item, talk, tags))
				.attr('target', '_blank')
		));

		row.append($('<span></span>').append(
			$('<button class="delete">Delete</button>')
		));

		row.append($('<span></span>').append(
			$('<button class="add-to-gallery">Add To Gallery</button>')
		));

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

			imageReader.readImage(fileInput).then(function(imageData) {
				return api({
					method: 'POST',
					path: root + '/items',
					entity: imageData,
					headers: {'Content-Type': 'application/json'}
				});
			}).then(function(response) {
				return api({
					method: 'GET',
					path: response.headers.Location
				});
			}).done(function(response) {
				var item = response.entity;
				items[item._links.self.href] = item;
				addItemRow(item);
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
			var itemUri = e.target.parentNode.parentNode.dataset['uri'];
			deletePic(items[itemUri]);
		});

		imagesEl.on('click', '.add-to-gallery', function(e) {
			var itemUri = e.target.parentNode.parentNode.dataset['uri'];
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

		var itemsReady = follow(api, root, [
			{ rel: 'items', params: { projection: "noImages"} },
			'search',
			{ rel: 'findByGalleryIsNull', params: { projection: "noImages" } },
			'items'])
			.then(function(response) {

				imagesEl.append(emptyImageTable);

				return when.map(response, function(itemWithoutImage) {
					return api({
						path: itemWithoutImage._links.self.href
					}).then(function(item) {
						items[item.entity._links.self.href] = item.entity;
						addItemRow(item.entity);
					});
				});
			});
	});
});
