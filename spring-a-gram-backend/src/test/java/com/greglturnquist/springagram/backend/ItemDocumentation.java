package com.greglturnquist.springagram.backend;

import static java.util.stream.Collectors.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.springframework.restdocs.RestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.LinkDiscoverers;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.config.RestDocumentationConfigurer;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
@SpringApplicationConfiguration(classes = ItemDocumentation.TestConfiguration.class)
public class ItemDocumentation {

	protected MockMvc mvc;
	protected static MediaType DEFAULT_MEDIA_TYPE = org.springframework.hateoas.MediaTypes.HAL_JSON;

	@Autowired WebApplicationContext context;
	@Autowired ItemRepository itemRepository;
	@Autowired UserRepository userRepository;
	@Autowired RepositoryEntityLinks entityLinks;
	@Autowired LinkDiscoverers discoverers;
	@Value("${spring.data.rest.basePath}") String basePath;

	@Before
	public void setUp() {

		mvc = MockMvcBuilders
				.webAppContextSetup(context)
				.apply(new RestDocumentationConfigurer())
				.defaultRequest(get("/").accept(DEFAULT_MEDIA_TYPE))
				.build();
		itemRepository.deleteAll();
	}

	@Test
	public void getACollectionOfItemsWithAProjection() throws Exception {

		Item newItem = new Item();
		newItem.setImage("test image");
		Item savedItem = itemRepository.save(newItem);

		Link itemsLink = entityLinks.linkToCollectionResource(Item.class);

		MvcResult result = mvc.perform(get(itemsLink.expand().getHref() + "?projection=noImages"))
				.andDo(document("getCollectionOfItemsWithNoImages"))
				.andDo(print())
				.andExpect(status().isOk())
				.andReturn();

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.registerModules(new Jackson2HalModule());
		PagedResources<Item> resourceItems = mapper.readValue(result.getResponse().getContentAsString(),
				new TypeReference<PagedResources<Item>>() {});

		assertThat(resourceItems.getLinks().size(), equalTo(2));

		assertThat(resourceItems.hasLink("self"), is(true));
		assertThat(resourceItems.getLink("self").isTemplated(), is(false));
		final String self = resourceItems.getLink("self").expand().getHref();
		assertThat(self, containsString(new URI(self).getPath()));

		assertThat(resourceItems.hasLink("search"), is(true));
		assertThat(resourceItems.getLink("search").isTemplated(), is(false));
		final String search = resourceItems.getLink("search").expand().getHref();
		assertThat(search, containsString(new URI(search).getPath()));

		Collection<Item> items = resourceItems.getContent();
		assertThat(items.size(), equalTo(1));
		Item item = items.toArray(new Item[]{})[0];
		assertThat(item.getImage(), is(nullValue()));
		assertThat(item.getGallery(), equalTo(savedItem.getGallery()));
		assertThat(item.getUser(), equalTo(savedItem.getUser()));
	}

	@Test
	public void hoppingFromRootToSingleItem() throws Exception {

		Item newItem = new Item();
		newItem.setImage("test image");
		Item savedItem = itemRepository.save(newItem);

		LinkDiscoverer linkDiscoverer = discoverers.getLinkDiscovererFor(MediaTypes.HAL_JSON);

		MvcResult rootResponse = mvc.perform(get(basePath))
				.andDo(document("hoppingFromRootToSingleItem.root"))
				.andExpect(status().isOk())
				.andReturn();

		Link itemsLink = linkDiscoverer.findLinkWithRel("items", rootResponse.getResponse().getContentAsString());

		MvcResult itemsResponse = mvc.perform(get(itemsLink.expand().getHref() + "?projection=noImages"))
				.andDo(document("hoppingFromRootToSingleItem.items"))
				.andExpect(status().isOk())
				.andReturn();

		Link searchLink = linkDiscoverer.findLinkWithRel("search", itemsResponse.getResponse().getContentAsString());

		MvcResult searchResponse = mvc.perform(get(searchLink.expand().getHref()))
				.andDo(document("hoppingFromRootToSingleItem.search"))
				.andExpect(status().isOk())
				.andReturn();

		Link findByGalleryIsNullLink = linkDiscoverer.findLinkWithRel("findByGalleryIsNull",
				searchResponse.getResponse().getContentAsString());

		MvcResult findByGalleryIsNullResponse = mvc.perform(get(findByGalleryIsNullLink.expand().getHref() + "?projection=noImages"))
				.andDo(document("hoppingFromRootToSingleItem.findByGalleryIsNull"))
				.andDo(print())
				.andExpect(status().isOk())
				.andReturn();

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.registerModules(new Jackson2HalModule());
		Resources<Resource<Item>> items = mapper.readValue(findByGalleryIsNullResponse.getResponse().getContentAsString(),
				new TypeReference<Resources<Resource<Item>>>() {
				});

		assertThat(items.getLinks().size(), equalTo(1));
		assertThat(items.getContent().size(), equalTo(1));

		List<Link> links = items.getContent().stream()
				.map(item -> item.getLink("self"))
				.collect(toList());

		List<Item> unlinkedItems = new ArrayList<>();

		for (Link link : links) {
			MvcResult result = mvc.perform(get(link.expand().getHref()))
					.andDo(document("hoppingFromRootToSingleItem" + new URI(link.expand().getHref()).getPath()))
					.andExpect(status().isOk())
					.andReturn();

			Resource<Item> itemResource = mapper.readValue(result.getResponse().getContentAsString(),
					new TypeReference<Resource<Item>>() {});
			unlinkedItems.add(itemResource.getContent());
		}

		assertThat(unlinkedItems.size(), equalTo(1));
		assertThat(unlinkedItems.get(0).getImage(), equalTo("test image"));
	}

	@Test
	public void createAndDestroy() throws Exception {

		String item = "{\"image\": \"test image\"}";

		// Security bits must be in place to support linking hte newly created item with the user
		User user = new User();
		user.setName("jack reacher");
		user.setPassword("1031");
		user.setRoles(new String[]{"ROLE_USER"});
		user = userRepository.save(user);

		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(user.getName(), user.getPassword()));

		Link itemsLink = entityLinks.linkToCollectionResource(Item.class);

		MvcResult createResults = mvc.perform(
				post(itemsLink.expand().getHref())
					.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					.content(item))
				.andDo(document("createAndDestroy.create"))
				.andExpect(status().isCreated())
				.andReturn();

		final String locationLink = createResults.getResponse().getHeader(HttpHeaders.LOCATION);
		assertThat(locationLink, is(notNullValue()));

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.registerModules(new Jackson2HalModule());

		Resource<Item> itemResource = mapper.readValue(createResults.getResponse().getContentAsString(),
				new TypeReference<Resource<Item>>() { });

		assertThat(itemResource.hasLink("self"), is(true));
		assertThat(itemResource.getLink("self").isTemplated(), is(true));
		assertThat(itemResource.getLink("self").expand().getHref(), equalTo(locationLink));

		mvc.perform(delete(locationLink))
				.andDo(document("createAndDestroy.delete"))
				.andExpect(status().isNoContent())
				.andReturn();

		mvc.perform(get(locationLink))
				.andDo(document("createAndDestroy.recheck"))
				.andExpect(status().isNotFound())
				.andReturn();
	}

	@Configuration
	@EnableJpaRepositories(basePackageClasses = Item.class)
	@EnableAutoConfiguration
	public static class TestConfiguration {

	}

}
