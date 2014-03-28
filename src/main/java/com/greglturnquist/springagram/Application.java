package com.greglturnquist.springagram;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.bind.DatatypeConverter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
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
@ComponentScan
public class Application {

	public static void main(String[] args) throws IOException {
		ApplicationContext ctx = SpringApplication.run(Application.class, args);

		// No demo is complete without pre-loading some cats

		ItemRepository itemRepository = ctx.getBean(ItemRepository.class);
		itemRepository.save(createItem(ctx.getResource("classpath:cat.jpg")));
		itemRepository.save(createItem(ctx.getResource("classpath:caterpillar.jpg")));

		GalleryRepository galleryRepository = ctx.getBean(GalleryRepository.class);
		Gallery gallery = new Gallery();
		gallery.setDescription("Collection of cats");
		galleryRepository.save(gallery);
	}

	private static Item createItem(Resource cat) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		FileCopyUtils.copy(cat.getInputStream(), output);
		Item item = new Item();
		item.setName(cat.getFilename());
		item.setImage("data:image/png;base64," +
				DatatypeConverter.printBase64Binary(output.toByteArray()));
		return item;
	}
}
