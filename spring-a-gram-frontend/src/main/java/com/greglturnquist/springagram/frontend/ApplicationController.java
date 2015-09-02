package com.greglturnquist.springagram.frontend;

import java.net.URI;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * This is the web controller that contains web pages and other custom end points.
 */
@Controller
public class ApplicationController {

	private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);

	private final RestTemplate rest = new RestTemplate();

	@Autowired
	ApplicationControllerHelper helper;

	@Value("${hashtag:#devnexus}")
	String hashtag;

	@Value("${spring.data.rest.basePath}")
	String basePath;

	/**
	 * Serve up the home page
	 * @return
	 */
	@RequestMapping(method=RequestMethod.GET, value="/")
	public ModelAndView index() {

		ModelAndView modelAndView = new ModelAndView("index");
		modelAndView.addObject("gallery", new Gallery());
		modelAndView.addObject("newGallery",
				linkTo(methodOn(ApplicationController.class).newGallery(null, null))
						.withRel("New Gallery"));
		return modelAndView;
	}

	@RequestMapping(method=RequestMethod.POST, value="/")
	public ModelAndView newGallery(@ModelAttribute Gallery gallery, HttpEntity<String> httpEntity) {

		Link root = linkTo(methodOn(ApplicationController.class).index()).slash("/api").withRel("root");
		Link galleries = new Traverson(URI.create(root.expand().getHref()), MediaTypes.HAL_JSON).//
				follow("galleries").//
				withHeaders(httpEntity.getHeaders()).//
				asLink();

		HttpHeaders headers = new HttpHeaders();
		headers.putAll(httpEntity.getHeaders());
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Object> subRequest = new HttpEntity<>(gallery, headers);
		rest.exchange(galleries.expand().getHref(), HttpMethod.POST, subRequest, Gallery.class);

		return index();
	}

	@RequestMapping(method=RequestMethod.GET, value="/image")
	public ModelAndView imageViaLink(@RequestParam("link") String link, HttpEntity<String> httpEntity) {

		Resource<Item> itemResource = helper.getImageResourceViaLink(link, httpEntity);

		return new ModelAndView("oneImage")
				.addObject("item", itemResource.getContent())
				.addObject("hashtag", hashtag)
				.addObject("links", Arrays.asList(
						linkTo(methodOn(ApplicationController.class).index()).withRel("All Images"),
						new Link(itemResource.getContent().getImage()).withRel("Raw Image"),
						new Link(link).withRel("HAL record")
				));
	}

}
