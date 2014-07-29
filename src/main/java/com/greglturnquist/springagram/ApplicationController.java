package com.greglturnquist.springagram;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

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
	public String index() {
		return "index";
	}

	/**
	 * Provide the RESTful means to fetch an individual image's data based on id
	 * @param id
	 * @return
	 */
	@RequestMapping("/image/{id}")
	public String image(@PathVariable Long id, Model model, HttpServletRequest request) {
		model.addAttribute("item", itemRepository.findOne(id));
		model.addAttribute("hashtag", hashtag);
		return "oneImage";
	}
}
