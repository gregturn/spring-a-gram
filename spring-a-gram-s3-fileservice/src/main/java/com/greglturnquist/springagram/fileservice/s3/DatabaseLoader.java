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

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * @author Greg Turnquist
 */
@Service
@Profile("!production")
public class DatabaseLoader {

	private final UserRepository userRepository;
	private final FileService fileService;
	private final ApplicationContext ctx;

	@Autowired
	public DatabaseLoader(UserRepository userRepository, FileService fileService, ApplicationContext ctx) {
		this.userRepository = userRepository;
		this.fileService = fileService;
		this.ctx = ctx;
	}

	@PostConstruct
	public void init() throws IOException {

		this.userRepository.deleteAll();

		User greg = new User();
		greg.setName("greg");
		greg.setPassword("turnquist");
		greg.setRoles(new String[]{"ROLE_USER"});
		userRepository.save(greg);

		User roy = new User();
		roy.setName("roy");
		roy.setPassword("clarkson");
		roy.setRoles(new String[]{"ROLE_USER"});
		userRepository.save(roy);

		this.fileService.deleteAll();

		loadImage("cat.jpg");
		loadImage("caterpillar.jpg");
	}

	private void loadImage(String filename) throws IOException {
		Resource resource = ctx.getResource("classpath:" + filename);
		this.fileService.saveFile(resource.getInputStream(), resource.getFile().length(), filename);
	}

}
