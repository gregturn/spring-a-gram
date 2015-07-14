define(function(require) {
	'use strict';

	var React = require('react');
	var client = require('./api');

	var FileForm = React.createClass({
		getInitialState: function() {
			return {data_uri: null};
		},
		handleSubmit: function(e) {
			e.preventDefault();
			var self = this;
			client({
				method: 'POST',
				path: self.props.url,
				entity: {image: self.state.data_uri},
				headers: {'Content-Type': 'application/json'}
			}).done(function(response) {
				self.setState({data_uri: null});
			});
		},
		handleFile: function(e) {
			var self = this;
			var reader = new FileReader();
			var file = e.target.files[0];

			reader.onload = function(upload) {
				self.setState({data_uri: upload.target.result});
			}

			reader.readAsDataURL(file);
		},
		render: function() {
			return (
				<form onSubmit={this.handleSubmit} encType="multipart/form-data">
					<input type="file" onChange={this.handleFile}></input>
					<input type="submit"></input>
				</form>
			)
		}
	});

	React.render(
		<FileForm url="/api/items" />,
		document.getElementById('upload')
	);

});