package com.greglturnquist.springagram;

import javax.annotation.PostConstruct;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

@Service
@Profile("!production")
public class DatabaseLoader {

	private final GalleryRepository galleryRepository;
	private final ItemRepository itemRepository;
	private final UserRepository userRepository;
	private final ApplicationContext ctx;

	@Autowired
	public DatabaseLoader(GalleryRepository galleryRepository, ItemRepository itemRepository,
			UserRepository userRepository, ApplicationContext ctx) {
		this.galleryRepository = galleryRepository;
		this.itemRepository = itemRepository;
		this.userRepository = userRepository;
		this.ctx = ctx;
	}

	@PostConstruct
	public void init() throws IOException {
		// No demo is complete without pre-loading some cats

		User greg = new User();
		greg.setName("greg");
		greg.setPassword("turnquist");
		greg.setRoles("ROLE_USER");
		userRepository.save(greg);

		User roy = new User();
		roy.setName("roy");
		roy.setPassword("clarkson");
		roy.setRoles("ROLE_USER");
		userRepository.save(roy);

		SecurityContextHolder.clearContext();

		runAs(roy.getName(), roy.getPassword(), "ROLE_USER");

		Item cat = itemRepository.save(createItem(ctx.getResource("classpath:cat.jpg"), roy));
		itemRepository.save(createItem(ctx.getResource("classpath:cat.jpg"), roy));

		runAs(greg.getName(), greg.getPassword(), "ROLE_USER");

		Item caterpillar = itemRepository.save(createItem(ctx.getResource("classpath:caterpillar.jpg"), greg));
		itemRepository.save(createItem(ctx.getResource("classpath:caterpillar.jpg"), greg));

		Gallery catGallery = new Gallery();
		catGallery.setDescription("Collection of cats");
		catGallery = galleryRepository.save(catGallery);

		Gallery truckGallery = new Gallery();
		truckGallery.setDescription("Collection of trucks");
		truckGallery = galleryRepository.save(truckGallery);

		// cat.setGallery(catGallery);
		// itemRepository.save(cat);

		// caterpillar.setGallery(truckGallery);
		// itemRepository.save(caterpillar);

		SecurityContextHolder.clearContext();
	}

	void runAs(String username, String password, String... roles) {
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(username, password, AuthorityUtils.createAuthorityList(roles)));
	}

	private static Item createItem(Resource file, User user) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		FileCopyUtils.copy(file.getInputStream(), output);
		Item item = new Item();
		item.setImage("data:image/png;base64," + DatatypeConverter.printBase64Binary(output.toByteArray()));
		item.setUser(user);
		return item;
	}

}
