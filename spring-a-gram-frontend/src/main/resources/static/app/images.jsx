define(function (require) {
	'use strict';

	require('./polyfill');

	var React = require('react');
	var client = require('./api');
	var stompClient = require('./websocket-listener');
	var _ = require('lodash');
	var twitter = require('./twitter');
	var linkHelper = require('./linkHelper');
	var hateoasHelper = require('./hateoasHelper');
	var when = require('when');
	var follow = require('./follow');

	var talk = "https://2015.event.springone2gx.com/schedule/sessions/spring_data_rest_data_meets_hypermedia_security.html on 9/15 @ 12:45pm";
	var tags = ['s2gx', 'REST'];

	var root = '/api';

	var FileForm = require('jsx!app/upload');
	var spinner = require('app/spinner');

	var ItemContainer = React.createClass({

		/**
		 * Load the initial state from the server.
		 */

		loadItemsFromServer: function () {
			follow(client, root, [
				'items',
				'search',
				{ rel: 'findByGalleryIsNull', params: { projection: 'owner'}}
			]).done(response => {
				this.setState({
					data: response.entity._embedded.items, galleries: this.state.galleries,
					selectedGallery: this.state.selectedGallery
				});
			}, response => {
				this.fallbackDataLoad();
			});
		},
		loadGalleriesFromServer: function () {
			follow(client, root, ['galleries']).done(galleries => {
				when.all(galleries.entity._embedded.galleries.map(this.loadItemsForGallery)).done(galleries => {
					this.setState({
						data: this.state.data,
						galleries: galleries,
						selectedGallery: this.state.selectedGallery
					});
				});
			},
			response => {
				this.fallbackDataLoad();
			});
		},
		loadItemsForGallery: function (gallery) {
			return client({
				method: 'GET',
				path: gallery._links.items.href,
				params: {projection: 'owner'}
			}).then(items => {
				return {
					gallery: gallery,
					items: items.entity._embedded.items
				};
			})
		},
		fallbackDataLoad: function() {
			this.setState({
				data: [{
					"image": "http://docs.spring.io/spring/docs/current/spring-framework-reference/htmlsingle/images/logo.png",
					"user": {"name": "Spring-a-Gram"},
					"_links": {
						"self": {
							"href": "http://localhost:8080/api/items/1"
						},
						"item": {
							"href": "http://localhost:8080/api/items/1{?projection}",
							"templated": true
						},
						"gallery": {
							"href": "http://localhost:8080/api/items/1/gallery"
						}
					}
				}],
				galleries: [],
				selectedGallery: undefined
			});
		},
		registerTopics: function () {
			stompClient.register([
				{route: '/topic/backend.newItem', callback: this.addItemToUnlinkedList},
				{route: '/topic/backend.deleteItem', callback: this.removeItemFromUnlinkedList},
				{route: '/topic/backend.removeItemFromGallery-item', callback: this.addItemToUnlinkedList},
				{route: '/topic/backend.removeItemFromGallery-gallery', callback: this.updateGallery},
				{route: '/topic/backend.addItemToGallery-item', callback: this.removeItemFromUnlinkedList},
				{route: '/topic/backend.addItemToGallery-gallery', callback: this.updateGallery}
			]);
			this.props.spinner.hideSpinner()
		},

		/**
		 * User-clicked callbacks
		 *
		 * These implement user requests. They aren't responsible for updating the state of the UI. Instead, when
		 * the operation completes on the server, a WebSocket message is expected. The handlers for those messages
		 * instead will update the UI's state.
		 */

		onSelectGallery: function (gallery) {
			this.setState({data: this.state.data, galleries: this.state.galleries, selectedGallery: gallery});
		},
		onAddToGallery: function (item) {
			if (this.state.selectedGallery === undefined) {
				alert("You haven't selected a gallery yet");
				return;
			}

			client({
				method: 'PUT',
				path: item._links.gallery.href,
				entity: this.state.selectedGallery,
				headers: {'Content-Type': 'text/uri-list'}
			}).done(() => {/* Let the websocket handler update the state */},
				response => {
					if (response.status.code === 403) {
						alert('You are not authorized to assign that picture to a gallery');
					}
				}
			);
		},
		onRemoveFromGallery: function (item) {
			client({
				method: 'DELETE',
				path: item._links.gallery.href
			}).done(() => {/* Let the websocket handler update the state */},
				response => {
					if (response.status.code === 403) {
						alert('You are not authorized to assign that picture to a gallery');
					}
				}
			);
		},
		onDelete: function (item) {
			client({
				method: 'DELETE',
				path: item.image
			}).then(response =>
				client({
					method: 'DELETE',
					path: item._links.self.href.split('{')[0]
				})
			).done(() => {/* Let the websocket handler update the state */},
				response => {
					if (response.status.code === 403) {
						alert('You are not authorized to delete that picture');
					}
				});
		},

		/**
		 * WebSocket-driven event handlers
		 *
		 * These handlers update the state of items and galleries. This way, the state is managed in one place, for
		 * both the screen that made the update and all other clients.
		 */

		addItemToUnlinkedList: function (message) {
			client({
				method: 'GET',
				path: message.body,
				params: {projection: "owner"}
			}).done(response => {
				this.setState({
					data: this.state.data.concat([response.entity]), galleries: this.state.galleries,
					selectedGallery: this.state.selectedGallery
				});
			})
		},
		removeItemFromUnlinkedList: function (message) {
			var items = this.state.data;
			_.remove(items, item => item._links.self.href.split('{')[0].endsWith(message.body));
			this.setState({data: items, galleries: this.state.galleries, selectedGallery: this.state.selectedGallery});
		},
		updateGallery: function (message) {
			client({
				method: 'GET',
				path: message.body
			}).then(response => {
				this.loadItemsForGallery(response.entity).done(refreshedGallery => {
					var newGalleries = this.state.galleries.map(gallery => {
						if (gallery.gallery._links.self.href === response.entity._links.self.href) {
							return refreshedGallery;
						} else {
							return gallery;
						}
					});
					this.setState({
						data: this.state.data,
						galleries: newGalleries,
						selectedGallery: this.state.selectedGallery
					});
				})
			})
		},

		/**
		 * ReactJS's standard API hooks.
		 */

		getInitialState: function () {
			return {data: [], galleries: [], selectedGallery: undefined}
		},

		componentDidMount: function () {
			when.try(this.loadItemsFromServer);
			when.try(this.loadGalleriesFromServer);
			when.try(this.registerTopics);
		},

		render: function () {
			return (
				<div>
					<section className="wrapper">
						<h3>Galleries</h3>

						<GalleryList galleries={this.state.galleries} onSelectGallery={this.onSelectGallery}
									 onRemoveFromGallery={this.onRemoveFromGallery}/>
					</section>

					<section className="wrapper">
						<hr/>
					</section>

					<section className="wrapper">
						<h3>Unlinked Images</h3>

						<ItemList data={this.state.data} onAddToGallery={this.onAddToGallery} onDelete={this.onDelete}/>
					</section>
				</div>
			)
		}
	});

	var GalleryList = React.createClass({
		render: function () {
			var galleries = this.props.galleries.map(gallery =>
				<Gallery gallery={gallery.gallery} items={gallery.items}
						 onSelectGallery={this.props.onSelectGallery}
						 onRemoveFromGallery={this.props.onRemoveFromGallery}
						 key={gallery.gallery._links.self.href}/>
			);
			return (
				<ul className="layout">
					{galleries}
				</ul>
			)
		}
	});

	var Gallery = React.createClass({
		handleChange: function (e) {
			this.props.onSelectGallery(this.props.gallery);
		},
		render: function () {
			var items = this.props.items.map(item =>
				<ItemInGallery item={item} gallery={this.props.gallery}
							   onRemoveFromGallery={this.props.onRemoveFromGallery}
							   key={item._links.self.href}/>
			);
			return (
				<li className="layout__item">
					<input className="btn--responsive" type="radio" onClick={this.handleChange}/>
					<span>{this.props.gallery.description}</span>
					<ul className="layout">
						{items}
					</ul>
				</li>
			)
		}
	});

	var ItemInGallery = React.createClass({
		handleRemove: function () {
			this.props.onRemoveFromGallery(this.props.item);
		},
		render: function () {
			return (
				<li className="layout__item lap-and-up-1/2 desk-1/3">
					<div className="media media--responsive box box--tiny">
						<img className="media__img palm-1/1 lap-and-up-1/2" src={this.props.item.image}/>

						<div className="media__body">
							<div className="layout">
								<a className="btn--responsive layout__item" target="_blank"
								   href={twitter.tweetIntent(this.props.item, talk, tags)}>Tweet</a>
								<button className="btn--responsive remove layout__item" onClick={this.handleRemove}>
									Remove
								</button>
							<a href={linkHelper.htmlUrl(this.props.item._links.self.href)}>
									<button className="btn--responsive permalink layout__item">Permalink</button>
								</a>
								<span className="layout__item">{this.props.item.user.name}</span>
							</div>
						</div>
					</div>
				</li>
			)
		}
	});

	var ItemList = React.createClass({
		render: function () {
			var items = this.props.data.map(item =>
				<Item item={item} onAddToGallery={this.props.onAddToGallery} onDelete={this.props.onDelete}
					  key={item._links.self.href}/>
			);
			return (
				<ul className="layout">
					{items}
				</ul>
			)
		}
	});

	var Item = React.createClass({
		handleAddToGallery: function () {
			this.props.onAddToGallery(this.props.item);
		},
		handleDelete: function () {
			this.props.onDelete(this.props.item);
		},
		render: function () {
			return (
				<li className="layout__item lap-and-up-1/2 desk-1/3" key={this.props.item._links.self.href}>
					<div className="media media--responsive box box--tiny">
						<img className="media__img palm-1/1 lap-and-up-1/2" src={this.props.item.image}/>

						<div className="media__body">
							<div className="layout">
								<a className="btn--responsive layout__item" target="_blank"
								   href={twitter.tweetIntent(this.props.item, talk, tags)}>Tweet</a>
								<button className="btn--responsive add-to-gallery layout__item"
										onClick={this.handleAddToGallery}>Add To Gallery
								</button>
								<button className="btn--responsive delete layout__item" onClick={this.handleDelete}>
									Delete
								</button>
								<a href={linkHelper.htmlUrl(this.props.item._links.self.href)}>
									<button className="btn--responsive permalink layout__item">Permalink</button>
								</a>
								<span className="layout__item">Uploaded by: {this.props.item.user.name}</span>
							</div>
						</div>
					</div>
				</li>
			)
		}
	})

	React.render(
		<FileForm url="/api/items" spinner={spinner} />,
		document.getElementById('upload')
	);

	React.render(
		<ItemContainer spinner={spinner} />,
		document.getElementById('react')
	);

});