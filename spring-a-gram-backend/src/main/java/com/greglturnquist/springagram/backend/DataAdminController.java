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
package com.greglturnquist.springagram.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Greg Turnquist
 */
@Controller
public class DataAdminController {

	private static final Logger log = LoggerFactory.getLogger(DataAdminController.class);

	private final GalleryRepository galleryRepository;
	private final ItemRepository itemRepository;

	@Autowired
	public DataAdminController(GalleryRepository galleryRepository, ItemRepository itemRepository) {
		this.galleryRepository = galleryRepository;
		this.itemRepository = itemRepository;
	}

	@RequestMapping("/")
	public String index() {
		return "index";
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(method = RequestMethod.POST, value = "/reset")
	public ResponseEntity<?> reset(Authentication authentication) {

		log.warn("!!! Resetting entire system as requested by " + authentication.getName());

		this.itemRepository.deleteAll();
		this.galleryRepository.deleteAll();

		return ResponseEntity.noContent().build();
	}

}
