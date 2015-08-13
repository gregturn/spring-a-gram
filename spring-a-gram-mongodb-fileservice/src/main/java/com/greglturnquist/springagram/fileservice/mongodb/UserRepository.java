package com.greglturnquist.springagram.fileservice.mongodb;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface UserRepository extends CrudRepository<User, String> {

	User findByName(String name);
}
