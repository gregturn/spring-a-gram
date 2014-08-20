package com.greglturnquist.springagram;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.util.FileCopyUtils;

@Configuration
@EnableJpaRepositories
@Import(CustomizedRestMvcConfiguration.class)
@EnableAutoConfiguration
@ComponentScan
public class Application {

	public static void main(String[] args) throws IOException {
		ApplicationContext ctx = SpringApplication.run(Application.class, args);

		// No demo is complete without pre-loading some cats

		ItemRepository itemRepository = ctx.getBean(ItemRepository.class);
		Item cat = itemRepository.save(createItem(ctx.getResource("classpath:cat.jpg")));
		Item caterpillar = itemRepository.save(createItem(ctx.getResource("classpath:caterpillar.jpg")));

		GalleryRepository galleryRepository = ctx.getBean(GalleryRepository.class);

		Gallery catGallery = new Gallery();
		catGallery.setDescription("Collection of cats");
		catGallery = galleryRepository.save(catGallery);

		Gallery truckGallery = new Gallery();
		truckGallery.setDescription("Collection of trucks");
		truckGallery = galleryRepository.save(truckGallery);

		cat.setGallery(catGallery);
		itemRepository.save(cat);

//		caterpillar.setGallery(truckGallery);
//		itemRepository.save(caterpillar);
	}

	private static Item createItem(Resource file) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		FileCopyUtils.copy(file.getInputStream(), output);
		Item item = new Item();
		item.setName(file.getFilename());
		item.setImage("data:image/png;base64," + DatatypeConverter.printBase64Binary(output.toByteArray()));
		return item;
	}
}
