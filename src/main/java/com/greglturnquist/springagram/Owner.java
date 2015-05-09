package com.greglturnquist.springagram;

import org.springframework.data.rest.core.config.Projection;
import org.springframework.hateoas.Link;

// tag::owner[]
@Projection(name = "owner", types = Item.class)
public interface Owner {

	public User getUser();

	public String getImage();

	public Link getHtmlUrl();

}
//end::owner[]
