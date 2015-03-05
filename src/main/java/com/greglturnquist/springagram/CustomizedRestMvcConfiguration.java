package com.greglturnquist.springagram;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;

/**
 * This subclass of {@link org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration}
 * is used to replace the baseUri of the hypermedia service.
 */
@Configuration
public class CustomizedRestMvcConfiguration extends RepositoryRestMvcConfiguration {

	@Override
	public RepositoryRestConfiguration config() {
		RepositoryRestConfiguration config = super.config();
		config.setBaseUri("/api");
		//config.setBasePath("/api");
		return config;
	}
}
