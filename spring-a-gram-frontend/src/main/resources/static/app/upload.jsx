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

			when.promise((resolve, reject) => {
				var request = new XMLHttpRequest();

				request.onerror = reject;
				request.onload = function () {
					resolve(request);
				}

				var selectedFile = React.findDOMNode(this.refs.fileInput).files[0];

				var formData = new FormData();
				formData.append("name", selectedFile.name);
				formData.append("file", selectedFile);

				request.open('POST', '/files');
				request.send(formData);
			}).then(response =>
				response.getResponseHeader("Location")
			).then(location => {
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
			}).done(response => {
				React.findDOMNode(this.refs.fileInput).value = null;
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
		document.getElementById('upload')
	);

});