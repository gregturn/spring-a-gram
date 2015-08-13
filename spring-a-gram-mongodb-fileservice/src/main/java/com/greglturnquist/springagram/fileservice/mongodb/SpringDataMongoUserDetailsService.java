package com.greglturnquist.springagram.fileservice.mongodb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class SpringDataMongoUserDetailsService implements UserDetailsService {

	private static final Logger log = LoggerFactory.getLogger(SpringDataMongoUserDetailsService.class);

	private UserRepository repository;

	@Autowired
	public SpringDataMongoUserDetailsService(UserRepository repository) {
		this.repository = repository;
	}

	@Override
	public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
		log.info("Fetching user " + s);
		User user = repository.findByName(s);
		log.info("Transforming " + user + " into UserDetails object");
		UserDetails userDetails = new org.springframework.security.core.userdetails.User(user.getName(), user.getPassword(),
				AuthorityUtils.createAuthorityList(user.getRoles()));
		log.info("About to return " + userDetails);
		return userDetails;
	}
}
