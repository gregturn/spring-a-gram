package com.greglturnquist.springagram;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface GalleryRepository extends CrudRepository<Gallery, Long> {
}
