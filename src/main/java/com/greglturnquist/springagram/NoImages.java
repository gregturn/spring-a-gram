package com.greglturnquist.springagram;

import org.springframework.data.rest.core.config.Projection;
import org.springframework.hateoas.Link;

/**
 * This interface defines a projection of the {@link Item} class. The annotation tags it, provides it a name and the
 * type it applies to. The getter methods define what exact attributes are let through.
 *
 * This will allow "curl localhost:8080/api/items?projection=noImages" to be run, and provide a slimmed down
 * JSON document with just the name and links provided. This applies anywhere an Item is retrieved.
 */
// tag::projection[]
@Projection(name = "noImages", types = {Item.class})
public interface NoImages {

	public Link getHtmlUrl();

}
// end::projection[]
