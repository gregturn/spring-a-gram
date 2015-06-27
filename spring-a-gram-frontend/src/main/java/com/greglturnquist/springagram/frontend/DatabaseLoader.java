package com.greglturnquist.springagram.frontend;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Profile("!production")
public class DatabaseLoader {

	private final UserRepository userRepository;

	@Autowired
	public DatabaseLoader(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@PostConstruct
	public void init() throws IOException {

		User greg = new User();
		greg.setName("greg");
		greg.setPassword("turnquist");
		greg.setRoles(new String[]{"ROLE_USER"});
		userRepository.save(greg);

		User roy = new User();
		roy.setName("roy");
		roy.setPassword("clarkson");
		roy.setRoles(new String[]{"ROLE_USER"});
		userRepository.save(roy);

		SecurityContextHolder.clearContext();
	}

}
