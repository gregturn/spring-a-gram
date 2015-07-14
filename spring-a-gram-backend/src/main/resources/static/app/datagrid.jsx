define(function(require) {
	'use strict';

	var React = require('react');
	var client = require('./api');
	var stompClient = require('./websocket-listener.js');
	var _ = require('lodash');

	var ItemTable = React.createClass({
		loadItemsFromServer: function() {
			var self = this;
			client({method: 'GET', path: this.props.url}).done(function(response) {
				self.setState({data: response.entity._embedded.items});
			})
		},
		getInitialState: function() {
			return {data: []}
		},
		componentDidMount: function() {
			this.loadItemsFromServer();
			stompClient.register([
				{ route: '/topic/backend.newItem', callback: this.showNewItem },
				{ route: '/topic/backend.deleteItem', callback: this.deleteItem }
			]);
		},
		showNewItem: function(message) {
			var href = message.body;
			var self = this;
			client({method: 'GET', path: href}).done(function(response) {
				var items = self.state.data;
				self.setState({data: items.concat([response.entity])});
			})
		},
		deleteItem: function(message) {
			var href = message.body;
			var items = this.state.data;
			_.remove(items, function(item) {
				return item._links.self.href.split('{')[0].endsWith(href);
			});
			this.setState({data: items});
		},
		onDelete: function(href) {
			client({method: 'DELETE', path: href.split('{')[0]}).done(function(response) {
				console.log("Deleting complete!");
			})
		},
		render: function() {
			var self = this;
			var items = this.state.data.map(function(item) {
				return (
					<Item _links={item._links} htmlUrl={item.htmlUrl} image={item.image} user={item.user}
						  onDelete={self.onDelete} key={item._links.self.href} />
				)
			})
			return (
				<table className="table table--cosy table--rows">
					<thead>
						<tr><th>Item</th><th>Owner</th><th>Links</th><th>Ops</th></tr>
					</thead>
					<tbody>
						{items}
					</tbody>
				</table>
			)
		}
	});

	var Item = React.createClass({
		render: function() {
			var self = this;
			var links = Object.keys(this.props._links).map(function(rel) {
				var uri = self.props._links[rel].href.split('{')[0];
				return (
					<li key={rel}><a href={uri}>{rel}</a></li>
				)
			})
			return (
				<tr>
					<td><img src={this.props.image} /></td>
					<td>{this.props.user.name}</td>
					<td>{links}</td>
					<td><DeleteButton _links={this.props._links} onDelete={this.props.onDelete} /></td>
				</tr>
			)
		}
	})

	var DeleteButton = React.createClass({
		handleClick: function() {
			this.props.onDelete(this.props._links.self.href);
		},
		render: function() {
			return (
				<button onClick={this.handleClick}>Delete</button>
			)
		}
	})

	React.render(
		<ItemTable url="/api/items?projection=owner" />,
		document.getElementById('grid')
	);

});