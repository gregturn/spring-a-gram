package com.greglturnquist.springagram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
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
@Component
@RepositoryEventHandler(Item.class)
public class PrepopulateUserInfoForItems {

	private static final Logger log = LoggerFactory.getLogger(PrepopulateUserInfoForItems.class);

	private final UserRepository repository;

	@Autowired
	public PrepopulateUserInfoForItems(UserRepository repository) {
		this.repository = repository;
	}


	@HandleBeforeCreate
	public void applyUserInformationUsingSecurityContext(Item item) {
		item.setUser(repository.findByName(SecurityContextHolder.getContext().getAuthentication().getName()));
	}

}
