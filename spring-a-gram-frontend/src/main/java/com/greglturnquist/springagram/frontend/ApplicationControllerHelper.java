package com.greglturnquist.springagram.frontend;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.TypeReferences;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ApplicationControllerHelper {

	private static final String QUERY_PROJECTION_OWNER = "?projection=owner";

	private final RestTemplate rest = new RestTemplate();

	@Value("${springagram.fallbackImageUrl:https://d1fto35gcfffzn.cloudfront.net/images/oss/oss-logo-spring.png}")
	private String fallbackImageUrl;

	@HystrixCommand(fallbackMethod = "getFallbackImageResource")
	public Resource<Item> getImageResourceViaLink(String link, HttpEntity<String> httpEntity) {
		String url = link + QUERY_PROJECTION_OWNER;
		HttpEntity<?> requestEntity = new HttpEntity<>(httpEntity.getHeaders());
		ParameterizedTypeReference<Resource<Item>> typeReference = new TypeReferences.ResourceType<Item>() {};
		ResponseEntity<Resource<Item>> resource = rest.exchange(url, HttpMethod.GET, requestEntity, typeReference);
		return resource.getBody();
	}

	public Resource<Item> getFallbackImageResource(String link, HttpEntity<String> httpEntity) {
		Item item = new Item();
		item.setImage(fallbackImageUrl);
		User user = new User();
		user.setName("fallback");
		user.setRoles(new String[]{});
		item.setUser(user);
		return new Resource<>(item);
	}

}
