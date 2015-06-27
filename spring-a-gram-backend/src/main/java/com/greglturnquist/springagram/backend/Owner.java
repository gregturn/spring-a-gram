package com.greglturnquist.springagram.backend;

import org.springframework.data.rest.core.config.Projection;

// tag::owner[]
@Projection(name = "owner", types = Item.class)
public interface Owner {

	public User getUser();

	public String getImage();

}
//end::owner[]
