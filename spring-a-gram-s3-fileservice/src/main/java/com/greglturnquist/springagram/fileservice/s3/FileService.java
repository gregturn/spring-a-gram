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
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.util.StringInputStream;

/**
 * @author Greg Turnquist
 */
@Service
public class FileService {

	private static final Logger log = LoggerFactory.getLogger(FileService.class);

	private final ResourcePatternResolver resourcePatternResolver;
	private final ResourceLoader resourceLoader;
	private final AmazonS3Client s3Client;

	@Value("${bucket}")
	private String bucket;

	@Autowired
	public FileService(ResourcePatternResolver resourcePatternResolver,
			ResourceLoader resourceLoader, AmazonS3Client s3Client) {

		this.resourcePatternResolver = resourcePatternResolver;
		this.resourceLoader = resourceLoader;
		this.s3Client = s3Client;
	}

	public void saveFile(InputStream input, long length, String filename) throws IOException {

		try {
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(length);
			this.s3Client.putObject(this.bucket, filename, input, metadata);
		}
		catch (AmazonS3Exception e) {
			if (e.getStatusCode() == 301) {
				updateEndpoint(e);
				saveFile(input, length, filename);
			}
		}
	}

	public Resource[] findAll() throws IOException {

		Resource[] results = new Resource[0];
		try {
			results = this.resourcePatternResolver.getResources(s3ify(this.bucket) + "/" + "*");
		}
		catch (AmazonS3Exception e) {
			if (e.getStatusCode() == 301) {
				updateEndpoint(e);
				results = this.findAll();
			}
		}
		return results;
	}

	public Resource findOne(String filename) {
		return this.resourceLoader.getResource(s3ify(this.bucket) + "/" + filename);
	}

	public void deleteAll() throws IOException {

		for (Resource resource : this.findAll()) {
			log.info("About to delete " + resource.getFilename());
			this.deleteOne(resource.getFilename());
		}
	}

	public void deleteOne(String filename) {
		this.s3Client.deleteObject(this.bucket, filename);
	}

	private String s3ify(String s) {
		if (s.startsWith("s3://")) {
			return s;
		} else {
			return "s3://" + s;
		}
	}

	/**
	 * Parse the {@link AmazonS3Exception} error result to capture the endpoint for
	 * redirection.
	 *
	 * @param e
	 */
	private void updateEndpoint(AmazonS3Exception e) {

		try {
			Document errorResponseDoc = DocumentBuilderFactory
					.newInstance()
					.newDocumentBuilder()
					.parse(new StringInputStream(e.getErrorResponseXml()));

			XPathExpression endpointXpathExtr = XPathFactory.newInstance().newXPath().compile("/Error/Endpoint");

			this.s3Client.setEndpoint(endpointXpathExtr.evaluate(errorResponseDoc));
		}
		catch (Exception ex) {
			throw new RuntimeException(e);
		}
	}

}
