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
					<p><input type="file" onChange={this.handleFile}></input></p>
					<p><input type="submit" value="Upload" className="btn btn--responsive"></input></p>
				</form>
			)
		}
	});

	React.render(
		<FileForm url="/api/items" />,
		document.getElementById('upload2')
	);

});