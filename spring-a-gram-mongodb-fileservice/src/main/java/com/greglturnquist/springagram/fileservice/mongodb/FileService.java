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

import static org.springframework.data.mongodb.core.query.Query.*;
import static org.springframework.data.mongodb.gridfs.GridFsCriteria.*;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

/**
 * @author Greg Turnquist
 */
@Service
public class FileService {

	private final GridFsTemplate gridFsTemplate;

	@Autowired
	public FileService(GridFsTemplate gridFsTemplate) {

		this.gridFsTemplate = gridFsTemplate;
	}

	public void saveFile(InputStream input, String filename) {

		this.gridFsTemplate.delete(query(whereFilename().is(filename)));
		this.gridFsTemplate.store(input, filename, MediaType.IMAGE_JPEG_VALUE);
	}

	public GridFsResource[] findAll() {
		return this.gridFsTemplate.getResources("*");
	}

	public GridFsResource findOne(String filename) {
		return this.gridFsTemplate.getResource(filename);
	}

	public void deleteAll() {

		for (GridFsResource resource : this.gridFsTemplate.getResources("*")) {
			this.deleteOne(resource.getFilename());
		}
	}

	public void deleteOne(String filename) {
		this.gridFsTemplate.delete(query(whereFilename().is(filename)));
	}
}
