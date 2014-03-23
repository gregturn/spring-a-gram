package com.greglturnquist.springagram;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.util.FileCopyUtils;

@Configuration
@EnableJpaRepositories
@Import(RepositoryRestMvcConfiguration.class)
@EnableAutoConfiguration
public class Application {

	public static void main(String[] args) throws IOException {
		ApplicationContext ctx = SpringApplication.run(Application.class, args);
		ItemRepository repository = ctx.getBean(ItemRepository.class);

		Resource catImage = ctx.getResource("classpath:cat.jpg");
		Item item = new Item();
		item.setName(catImage.getFilename());
		item.setImage(FileCopyUtils.copyToByteArray(catImage.getFile()));
		repository.save(item);
	}
}
