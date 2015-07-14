package com.greglturnquist.springagram.backend;

import static com.greglturnquist.springagram.backend.WebSocketConfiguration.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
 * hold up {@link Item} creation until the {@link User}
 * can be retrieved and populated.
 *
 * Since this is in the same thread of execution as the original REST call,
 * {@link SecurityContextHolder} can be used to retrieve the username,
 * and hence do the user lookup.
 */
// tag::event-handler-one[]
@Component
@RepositoryEventHandler(Item.class)
public class SpringDataRestEventHandler {
// end::event-handler-one[]

	private static final Logger log = LoggerFactory.getLogger(SpringDataRestEventHandler.class);

	private final UserRepository repository;
	private final RabbitTemplate rabbit;
	private final EntityLinks entityLinks;
	private final ResourceMappings resourceMappings;
	private final RepositoryRestConfiguration config;
	private final SimpMessagingTemplate websocket;

	@Autowired
	public SpringDataRestEventHandler(UserRepository repository, RabbitTemplate rabbit, EntityLinks entityLinks,
									  ResourceMappings resourceMappings, RepositoryRestConfiguration config,
									  SimpMessagingTemplate websocket) {

		this.repository = repository;
		this.rabbit = rabbit;
		this.entityLinks = entityLinks;
		this.resourceMappings = resourceMappings;
		this.config = config;
		this.websocket = websocket;
	}

	// tag::event-handler-two[]
	@HandleBeforeCreate
	public void applyUserInformationUsingSecurityContext(Item item) {

		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = repository.findByName(name);
		if (user == null) {
			User newUser = new User();
			newUser.setName(name);
			user = repository.save(newUser);
		}
		item.setUser(user);
	}
	// end::event-handler-two[]

	// tag::event-handler-three[]
	@HandleAfterCreate
	public void notifyAllClientsAboutNewItem(Item item) {

		log.info("Just created new item " + item);
		publish("backend.newItem", pathFor(item));
	}

	@HandleAfterDelete
	public void notifyAllClientsAboutItemDeletion(Item item) {

		log.info("Just deleted item " + item);
		publish("backend.deleteItem", pathFor(item));
	}
	// end::event-handler-three[]

	@HandleAfterLinkDelete
	public void notifyAllClientsWhenRemovedFromGallery(Item item, Object obj) {

		log.info("Item " + item + " just had an afterLinkDelete...");
		log.info("Related object => " + obj);
		publish("backend.removeItemFromGallery-item", pathFor(item));
		publish("backend.removeItemFromGallery-gallery", pathFor((Gallery) obj));
	}

	@HandleAfterLinkSave
	public void notifyAllClientsWhenAddedToGallery(Item item, Object obj) {

		log.info("Item " + item + " just had an afterLinkSave...");
		publish("backend.addItemToGallery-item", pathFor(item));
		publish("backend.addItemToGallery-gallery", pathFor(item.getGallery()));
	}

	private void publish(String routingKey, String message) {

		rabbit.convertAndSend(RabbitConfig.EXCHANGE, routingKey, message);
		websocket.convertAndSend(MESSAGE_PREFIX + "/" + routingKey, message);
	}

	// tag::event-handler-four[]
	private String pathFor(Item item) {

		return entityLinks.linkForSingleResource(item.getClass(),
				item.getId()).toUri().getPath();
	}
	// end::event-handler-four[]

	private String pathFor(Gallery gallery) {

		return entityLinks.linkForSingleResource(gallery.getClass(),
				gallery.getId()).toUri().getPath();
	}

}
