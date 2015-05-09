package com.greglturnquist.springagram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleAfterLinkDelete;
import org.springframework.data.rest.core.annotation.HandleAfterLinkSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.hateoas.EntityLinks;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Because the published application context events are, in fact, synchronous, this handler is able to
 * hold up {@link com.greglturnquist.springagram.Item} creation until the {@link com.greglturnquist.springagram.User}
 * can be retrieved and populated.
 *
 * Since this is in the same thread of execution as the original REST call,
 * {@link org.springframework.security.core.context.SecurityContextHolder} can be used to retrieve the username,
 * and hence do the user lookup.
 */
// tag::event-handler-one[]
@Component
@RepositoryEventHandler(Item.class)
public class SpringDataRestEventHandler {
// end::event-handler-one[]

	private static final Logger log = LoggerFactory.getLogger(SpringDataRestEventHandler.class);

	private final UserRepository repository;
	private final SimpMessagingTemplate template;
	private final EntityLinks entityLinks;
	@Autowired private ResourceMappings resourceMappings;
	@Autowired RepositoryRestConfiguration config;

	@Autowired
	public SpringDataRestEventHandler(UserRepository repository, SimpMessagingTemplate template,
									  EntityLinks entityLinks) {
		this.repository = repository;
		this.template = template;
		this.entityLinks = entityLinks;
	}

	// tag::event-handler-two[]
	@HandleBeforeCreate
	public void applyUserInformationUsingSecurityContext(Item item) {
		item.setUser(repository.findByName(
			SecurityContextHolder.getContext().getAuthentication().getName()));
	}
	// end::event-handler-two[]

	// tag::event-handler-three[]
	@HandleAfterCreate
	public void notifyAllClientsAboutNewItem(Item item) {
		template.convertAndSend(WebSocketConfiguration.MESSAGE_PREFIX + "/newItem", pathFor(item));
	}

	@HandleAfterDelete
	public void notifyAllClientsAboutItemDeletion(Item item) {
		template.convertAndSend(WebSocketConfiguration.MESSAGE_PREFIX + "/deleteItem", pathFor(item));
	}
	// end::event-handler-three[]

	@HandleAfterLinkDelete
	public void notifyAllClientsWhenRemovedFromGallery(Item item, Object obj) {
		log.info("Item " + item + " just had an afterLinkDelete...");
		log.info("Related object => " + obj);
		template.convertAndSend(WebSocketConfiguration.MESSAGE_PREFIX + "/removeItemFromGallery", pathFor(item));
	}

	@HandleAfterLinkSave
	public void notifyAllClientsWhenAddedToGallery(Item item, Object obj) {
		log.info("Item " + item + " just had an afterLinkSave...");
		log.info("Related object => " + obj);
		template.convertAndSend(WebSocketConfiguration.MESSAGE_PREFIX + "/addItemToGallery", pathFor(item));
	}

	// tag::event-handler-four[]
	private String pathFor(Item item) {
		return entityLinks.linkForSingleResource(item.getClass(), item.getId()).toUri().getPath();
	}
	// end::event-handler-four[]

}
