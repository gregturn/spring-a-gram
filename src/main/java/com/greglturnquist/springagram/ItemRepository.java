package com.greglturnquist.springagram;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource()
public interface ItemRepository extends PagingAndSortingRepository<Item, Long> {
}
