define(function (require) {
	'use strict';

	var React = require('react');
	var client = require('./api');
	var when = require('when');
	var follow = require('./follow');

	var root = '/api';

	var FileForm = React.createClass({
		handleSubmit: function (e) {
			e.preventDefault();
			var self = this;

			when.promise(function (resolve, reject) {
				var request = new XMLHttpRequest();

				request.onerror = reject;
				request.onload = function () {
					resolve(request);
				}

				var selectedFile = React.findDOMNode(self.refs.fileInput).files[0];

				var formData = new FormData();
				formData.append("name", selectedFile.name);
				formData.append("file", selectedFile);

				request.open('POST', '/upload');
				request.send(formData);
			}).then(function (response) {
				return response.getResponseHeader("Location");
			}).then(function (location) {
				if (location === null) {
					return "No location header";
				}
				return follow(client, root, ['items']).then(function(response) {
					return client({
						method: 'POST',
						path: response.entity._links.self.href,
						entity: {image: location},
						headers: {'Content-Type': 'application/json'}
					});
				});
			}).done(function (response) {
				React.findDOMNode(self.refs.fileInput).value = null;
			});
		},
		render: function () {
			return (
				<form onSubmit={this.handleSubmit} encType="multipart/form-data">
					<p><input type="file" ref="fileInput"></input></p>
					<p><input type="submit" value="Upload" className="btn btn--responsive"></input></p>
				</form>
			)
		}
	});

	React.render(
		<FileForm url="/api/items"/>,
		document.getElementById('upload2')
	);

});