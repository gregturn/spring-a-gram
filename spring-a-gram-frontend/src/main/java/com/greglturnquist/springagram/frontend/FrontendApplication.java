package com.greglturnquist.springagram.frontend;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@SpringBootApplication
@EnableDiscoveryClient
@EnableZuulProxy
public class FrontendApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(FrontendApplication.class, args);
	}

}
