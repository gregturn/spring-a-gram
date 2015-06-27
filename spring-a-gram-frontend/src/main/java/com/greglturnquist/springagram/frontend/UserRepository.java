package com.greglturnquist.springagram.frontend;

import org.springframework.data.repository.Repository;

/**
 * Repository to support Spring Security/Spring Data JPA
 */
public interface UserRepository extends Repository<User, Long> {

	User save(User user);

	User findByName(String name);

}
