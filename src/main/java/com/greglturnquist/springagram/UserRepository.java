package com.greglturnquist.springagram;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

// tag::user-repository[]
@RepositoryRestResource(exported = false)
public interface UserRepository extends CrudRepository<User, Long> {

	User findByName(String name);
}
// end::user-repository[]
