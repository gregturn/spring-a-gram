package com.greglturnquist.springagram.fileservice.mongodb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MongoDbFileServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MongoDbFileServiceApplication.class, args);
    }
}
