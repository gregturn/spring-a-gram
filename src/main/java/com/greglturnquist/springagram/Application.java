package com.greglturnquist.springagram;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
@Import(CustomizedRestMvcConfiguration.class)
public class Application {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(Application.class, args);
	}

}

