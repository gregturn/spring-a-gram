/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.greglturnquist.springagram.fileservice.mongodb;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author Greg Turnquist
 */
@RestController
public class ApplicationController {

	private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);

	private final FileService fileService;
	private final UserRepository userRepository;

	@Autowired
	public ApplicationController(FileService fileService, UserRepository userRepository) {
		this.fileService = fileService;
		this.userRepository = userRepository;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/files")
	public ResponseEntity<?> newFile(@RequestParam("name") String filename, @RequestParam("file") MultipartFile file) {

		if (!file.isEmpty()) {
			try {
				this.fileService.saveFile(file.getInputStream(), filename);

				Link link = linkTo(methodOn(ApplicationController.class).getFile(filename)).withRel(filename);
				return ResponseEntity.created(new URI(link.getHref())).build();

			} catch (IOException | URISyntaxException e) {
				return ResponseEntity.badRequest().body("Couldn't process the request");
			}
		} else {
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("File is empty");
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/files")
	public ResponseEntity<ResourceSupport> listAllFiles() {

		ResourceSupport files = new ResourceSupport();

		for (GridFsResource resource : this.fileService.findAll()) {
			files.add(linkTo(methodOn(ApplicationController.class).getFile(resource.getFilename()))
					.withRel(resource.getFilename()));
		}

		return ResponseEntity.ok(files);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/files/{filename}")
	public ResponseEntity<?> getFile(@PathVariable String filename) {

		GridFsResource file = this.fileService.findOne(filename);

		if (file == null) {
			return ResponseEntity.notFound().build();
		}

		try {
			return ResponseEntity.ok().contentLength(file.contentLength())
					.contentType(MediaType.parseMediaType(file.getContentType()))
					.body(new InputStreamResource(file.getInputStream()));
		}
		catch (IOException e) {
			return ResponseEntity.badRequest().body("Couldn't process the request");
		}
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/files/{filename}")
	public ResponseEntity<?> deleteFile(@PathVariable String filename) {

		this.fileService.deleteOne(filename);

		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(method = RequestMethod.POST, value = "/reset")
	public ResponseEntity<?> reset(Authentication authentication) {

		log.warn("!!! Resetting entire system as requested by " + authentication.getName());
		this.fileService.deleteAll();
		try {
			log.warn("!!! Reload user details as requested by " + authentication.getName());
			new SecurityDetailsLoader(userRepository).init();
		} catch (IOException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}

		return ResponseEntity.noContent().build();
	}

	/**
	 * Suffixes like ".jpg" should be part of the path and not extracted for content negotiation.
	 */
	@Configuration
	static class AllResources extends WebMvcConfigurerAdapter {

		@Override
		public void configurePathMatch(PathMatchConfigurer matcher) {
			matcher.setUseSuffixPatternMatch(false);
		}

	}
}
