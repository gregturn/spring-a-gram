package com.greglturnquist.springagram.backend;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.springframework.restdocs.RestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.net.URI;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.MediaType;
import org.springframework.restdocs.config.RestDocumentationConfigurer;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringApplicationConfiguration(classes = GalleryDocumentation.TestConfiguration.class)
public class GalleryDocumentation {

	protected MockMvc mvc;
	protected static MediaType DEFAULT_MEDIA_TYPE = org.springframework.hateoas.MediaTypes.HAL_JSON;

	@Autowired WebApplicationContext context;
	@Autowired GalleryRepository galleryRepository;
	@Autowired RepositoryEntityLinks entityLinks;

	@Before
	public void setUp() {

		mvc = MockMvcBuilders
				.webAppContextSetup(context)
				.apply(new RestDocumentationConfigurer())
				.defaultRequest(get("/").accept(DEFAULT_MEDIA_TYPE))
				.build();
	}

	@Test
	public void getACollectionOfGalleries() throws Exception {

		Gallery newGallery = new Gallery();
		newGallery.setDescription("Collection of cats");
		Gallery savedGallery = galleryRepository.save(newGallery);

		Link galleriesLink = entityLinks.linkToCollectionResource(Gallery.class);

		MvcResult result = mvc.perform(get(galleriesLink.expand().getHref()))
				.andDo(print())
				.andDo(document("getCollectionOfGalleries"))
				.andExpect(status().isOk())
				.andReturn();

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.registerModules(new Jackson2HalModule());
		PagedResources<Gallery> resourceGalleries = mapper.readValue(result.getResponse().getContentAsString(),
				new TypeReference<PagedResources<Gallery>>() {});

		assertThat(resourceGalleries.getLinks().size(), equalTo(1));

		assertThat(resourceGalleries.hasLink("self"), is(true));
		assertThat(resourceGalleries.getLink("self").isTemplated(), is(false));
		final String self = resourceGalleries.getLink("self").expand().getHref();
		assertThat(self, containsString(new URI(self).getPath()));

		Collection<Gallery> galleries = resourceGalleries.getContent();
		assertThat(galleries.size(), equalTo(1));
		Gallery gallery = galleries.toArray(new Gallery[]{})[0];
		assertThat(gallery.getItems(), is(nullValue()));
		assertThat(gallery.getDescription(), equalTo(savedGallery.getDescription()));
	}

	@Configuration
	@EnableJpaRepositories(basePackageClasses = Item.class)
	@EnableAutoConfiguration
	public static class TestConfiguration {

	}

}
