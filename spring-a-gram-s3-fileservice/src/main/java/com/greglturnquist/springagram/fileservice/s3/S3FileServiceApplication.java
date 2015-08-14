package com.greglturnquist.springagram.fileservice.s3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class S3FileServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(S3FileServiceApplication.class, args);
    }
}
