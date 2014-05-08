package com.greglturnquist.springagram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * This is the web controller that contains web pages and other custom end points.
 */
@Controller
public class ApplicationController {

	@Autowired
	ItemRepository itemRepository;

	/**
	 * Serve up the home page
	 * @return
	 */
	@RequestMapping("/index")
	public String index() {
		return "index";
	}

	/**
	 * Provide the RESTful means to fetch an individual image's data based on id
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/image/{id}")
	public String image(@PathVariable Long id) {
		return itemRepository.findOne(id).getImage();
	}
}
