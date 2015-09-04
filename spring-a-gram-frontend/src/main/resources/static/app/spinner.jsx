define(function (require) {
	'use strict';

	var React = require('react');

	function showSpinner() {

	}

	var Spinner = React.createClass({

		showSpinner: function () {
			console.log('Show the spinner...');
			React.findDOMNode(this.refs.spinner).style.cssText = '';
		},
		hideSpinner: function () {
			console.log('Hide the spinner...');
			React.findDOMNode(this.refs.spinner).style.cssText = 'display: none;';
		},

		render: function () {
			return (
				<div ref="spinner" className="loader" style={{display: 'none'}}>Loading...</div>
			)
		}

	});

	return Spinner;

});