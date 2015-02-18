package com.greglturnquist.springagram;

import org.springframework.data.rest.core.config.Projection;

@Projection(name = "owner", types = Item.class)
public interface Owner {

	public User getUser();

}
