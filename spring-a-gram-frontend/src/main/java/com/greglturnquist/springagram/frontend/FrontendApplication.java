package com.greglturnquist.springagram.frontend;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@SpringBootApplication
@EnableZuulProxy
public class FrontendApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(FrontendApplication.class, args);
	}

}

