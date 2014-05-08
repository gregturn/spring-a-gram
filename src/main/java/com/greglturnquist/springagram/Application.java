package com.greglturnquist.springagram;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.mobile.device.view.LiteDeviceDelegatingViewResolver;
import org.springframework.util.FileCopyUtils;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

@Configuration
@EnableJpaRepositories
@Import(RepositoryRestMvcConfiguration.class)
@EnableAutoConfiguration
@ComponentScan
public class Application {

	@Autowired
	private SpringTemplateEngine templateEngine;

	@Bean
	public LiteDeviceDelegatingViewResolver thymeleafViewResolver() {
		ThymeleafViewResolver delegate = new ThymeleafViewResolver();
		delegate.setTemplateEngine(this.templateEngine);
		delegate.setCharacterEncoding("UTF-8");
		LiteDeviceDelegatingViewResolver resolver = new LiteDeviceDelegatingViewResolver(delegate);
		resolver.setMobilePrefix("mobile/");
		resolver.setTabletPrefix("tablet/");
		return resolver;
	}

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

	private static Item createItem(Resource cat) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		FileCopyUtils.copy(cat.getInputStream(), output);
		Item item = new Item();
		item.setName(cat.getFilename());
		item.setImage("data:image/png;base64," + DatatypeConverter.printBase64Binary(output.toByteArray()));
		return item;
	}
}
