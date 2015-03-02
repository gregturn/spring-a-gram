var swagger = {
  "swagger": "2.0",
  "info": {
    "description": "These are all the links for form controls from an Amazon web page",
    "version": "1.0.0",
    "title": "Amazon"
  },
  "paths": {

  }
};

var links = document.querySelectorAll('a');
for (var i = 0; i < links.length; i++) {
	var link = links[i].href;
	var path = "/" + link.split('/').slice(3,-1).join('/')
	var stuff = {
		"description" : link,
		"responses": {
			"200": {
				"description": "success"
			}
		}
	};
	if (i % 2 === 0) {
		swagger.paths[path] = {
			"get": stuff
		}
	} else {
		swagger.paths[path] = {
			"post": stuff
		}
	}
	links[i].remove();
}
var forms = document.querySelectorAll('form');
for (var i = 0; i < forms.length; i++) {
	forms[i].remove();
}
console.log(JSON.stringify(swagger, null, 2));