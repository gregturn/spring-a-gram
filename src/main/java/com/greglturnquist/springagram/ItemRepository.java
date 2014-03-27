package com.greglturnquist.springagram;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource()
public interface ItemRepository extends PagingAndSortingRepository<Item, Long> {

	List<Item> findByGalleryIsNull();

}
