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
package com.greglturnquist.springagram.fileservice.s3;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

	@Autowired
	public ApplicationController(FileService fileService) {
		this.fileService = fileService;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/files")
	public ResponseEntity<?> newFile(@RequestParam("name") String filename, @RequestParam("file") MultipartFile file) {

		try {
			this.fileService.saveFile(file.getInputStream(), file.getSize(), filename);

			Link link = linkTo(methodOn(ApplicationController.class).getFile(filename)).withRel(filename);
			return ResponseEntity.created(new URI(link.getHref())).build();

		} catch (IOException | URISyntaxException e) {
			return ResponseEntity.badRequest().body("Couldn't process the request");
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/files")
	public ResponseEntity<?> listFiles() {

		try {
			Resource[] files = this.fileService.findAll();

			ResourceSupport resources = new ResourceSupport();

			for (Resource file : files) {
				resources.add(linkTo(methodOn(ApplicationController.class).getFile(file.getFilename()))
						.withRel(file.getFilename()));
			}

			return ResponseEntity.ok(resources);
		} catch (IOException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/files/{filename}")
	public ResponseEntity<?> getFile(@PathVariable String filename) throws IOException {

		Resource file = this.fileService.findOne(filename);

		try {
			return ResponseEntity.ok().contentLength(file.contentLength())
					.contentType(MediaType.IMAGE_JPEG)
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

	@Configuration
	static class AllResources extends WebMvcConfigurerAdapter {

		@Override
		public void configurePathMatch(PathMatchConfigurer matcher) {
			matcher.setUseSuffixPatternMatch(false);
		}

	}

}
