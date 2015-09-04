package com.greglturnquist.springagram.backend;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SecurityDetailsLoader {

	private final UserRepository userRepository;

	@Autowired
	public SecurityDetailsLoader(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@PostConstruct
	public void init() throws IOException {

		userRepository.findAll().forEach(userRepository::delete);

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
	}

}
