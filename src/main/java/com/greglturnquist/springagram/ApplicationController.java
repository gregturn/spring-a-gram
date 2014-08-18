package com.greglturnquist.springagram;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * This is the web controller that contains web pages and other custom end points.
 */
@Controller
public class ApplicationController {

	private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);

	@Autowired
	ItemRepository itemRepository;

	@Value("${hashtag:#s2gx}")
	String hashtag;

	/**
	 * Serve up the home page
	 * @return
	 */
	@RequestMapping("/")
	public ModelAndView index() {
		return new ModelAndView("index");
	}

	/**
	 * Provide the RESTful means to fetch an individual image's data based on id
	 * @param id
	 * @return
	 */
	@RequestMapping("/image/{id}")
	public ModelAndView image(@PathVariable Long id) {
		ModelAndView modelAndView = new ModelAndView("oneImage");
		modelAndView.addObject("item", itemRepository.findOne(id));
		modelAndView.addObject("hashtag", hashtag);
		modelAndView.addObject("links", Arrays.asList(
			linkTo(methodOn(ApplicationController.class).index()).withRel("All Images")
		));
		return modelAndView;
	}
}
