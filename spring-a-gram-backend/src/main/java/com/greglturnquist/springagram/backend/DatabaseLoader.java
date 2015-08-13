package com.greglturnquist.springagram.backend;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.xml.bind.DatatypeConverter;

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

	/**
	 * Demo the application by pre-loading some cats (but only in a development environemnt)
	 *
	 * @throws IOException
	 */
	@PostConstruct
	public void init() throws IOException {

		User reacher = new User();
		reacher.setName("jack");
		reacher.setPassword("reacher");
		reacher.setRoles(new String[]{"ROLE_USER", "ROLE_ADMIN"});
		reacher = userRepository.save(reacher);

		User strange = new User();
		strange.setName("doctor");
		strange.setPassword("strange");
		strange.setRoles(new String[]{"ROLE_USER"});
		strange = userRepository.save(strange);

		SecurityContextHolder.clearContext();

//		runAs(strange.getName(), strange.getPassword(), "ROLE_USER");

//		Item cat = itemRepository.save(createItem(ctx.getResource("classpath:cat.jpg"), strange));
		//itemRepository.save(createItem(ctx.getResource("classpath:cat.jpg"), strange));

		runAs(reacher.getName(), reacher.getPassword(), "ROLE_USER");

//		Item caterpillar = itemRepository.save(createItem(ctx.getResource("classpath:caterpillar.jpg"), reacher));
		//itemRepository.save(createItem(ctx.getResource("classpath:caterpillar.jpg"), reacher));

		Gallery catGallery = galleryRepository.save(new Gallery("Collection of cats"));
		Gallery truckGallery = galleryRepository.save(new Gallery("Collection of trucks"));

//		 cat.setGallery(catGallery);
//		 itemRepository.save(cat);

//		 caterpillar.setGallery(truckGallery);
//		 itemRepository.save(caterpillar);

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
