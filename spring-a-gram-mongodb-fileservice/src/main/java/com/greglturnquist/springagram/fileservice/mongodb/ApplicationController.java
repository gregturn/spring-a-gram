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

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author Greg Turnquist
 */
@Controller
public class ApplicationController {

	private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);

	private final FileService fileService;

	@Autowired
	public ApplicationController(FileService fileService) {
		this.fileService = fileService;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/upload")
	public void newFile(@RequestParam("name") String filename,
			@RequestParam("file") MultipartFile file, HttpServletResponse response) {

		if (!file.isEmpty()) {
			try {
				this.fileService.saveFile(file.getInputStream(), filename);
				Link link = linkTo(methodOn(ApplicationController.class).getFile(filename)).withRel(filename);
				response.addHeader(HttpHeaders.LOCATION, link.getHref());
			} catch (IOException e) {
				throw new UnableToProcessFileException();
			}
		} else {
			throw new EmptyFileException();
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/files")
	@ResponseBody
	public ResponseEntity<ResourceSupport> listAllFiles() {

		ResourceSupport files = new ResourceSupport();

		for (GridFsResource resource : this.fileService.findAll()) {
			files.add(linkTo(methodOn(ApplicationController.class).getFile(resource.getFilename()))
					.withRel(resource.getFilename()));
		}

		return ResponseEntity.ok(files);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/files/{filename}")
	@ResponseBody
	public ResponseEntity<InputStreamResource> getFile(@PathVariable String filename) {

		System.out.println("Looking for " + filename);

		GridFsResource file = this.fileService.findOne(filename);

		try {
			return ResponseEntity.ok().contentLength(file.contentLength())
					.contentType(MediaType.parseMediaType(file.getContentType()))
					.body(new InputStreamResource(file.getInputStream()));
		}
		catch (IOException e) {
			throw new UnableToProcessFileException();
		}
	}

	@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE, reason = "File is empty")
	static class EmptyFileException extends RuntimeException {
	}

	@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Couldn't process the request")
	static class UnableToProcessFileException extends RuntimeException {
	}

	@Configuration
	static class AllResources extends WebMvcConfigurerAdapter {

		@Override
		public void configurePathMatch(PathMatchConfigurer matcher) {
			matcher.setUseSuffixPatternMatch(false);
		}

	}
}
