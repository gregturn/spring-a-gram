package com.greglturnquist.springagram;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * This is the web controller that contains web pages and other custom end points.
 */
@Controller
public class ApplicationController {

	private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);

	private final ItemRepository itemRepository;
	private final GalleryRepository galleryRepository;

	@Autowired
	public ApplicationController(ItemRepository itemRepository, GalleryRepository galleryRepository) {
		this.itemRepository = itemRepository;
		this.galleryRepository = galleryRepository;
	}

	@Value("${hashtag:#devnexus}")
	String hashtag;

	/**
	 * Serve up the home page
	 * @return
	 */
	@RequestMapping(value="/", method=RequestMethod.GET)
	public ModelAndView index() {
		ModelAndView modelAndView = new ModelAndView("index");
		modelAndView.addObject("gallery", new Gallery());
		modelAndView.addObject("newGallery",
			linkTo(methodOn(ApplicationController.class).newGallery(null))
				.withRel("New Gallery"));
		return modelAndView;
	}

	@RequestMapping(value="/", method = RequestMethod.POST)
	public ModelAndView newGallery(@ModelAttribute Gallery gallery) {
		galleryRepository.save(gallery);
		return index();
	}

	/**
	 * Provide the RESTful means to fetch an individual image's data based on id
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/image/{id}", method=RequestMethod.GET)
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
