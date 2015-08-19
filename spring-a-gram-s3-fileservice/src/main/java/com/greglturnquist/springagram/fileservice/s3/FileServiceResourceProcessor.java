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

import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

/**
 * Add an extra root link to the controller that lists all files.
 *
 * @author Greg Turnquist
 */
@Component
public class FileServiceResourceProcessor implements ResourceProcessor<RepositoryLinksResource> {

	@Override
	public RepositoryLinksResource process(RepositoryLinksResource resources) {

		resources.add(linkTo(methodOn(ApplicationController.class).listFiles()).withRel("files"));

		return resources;
	}
}
