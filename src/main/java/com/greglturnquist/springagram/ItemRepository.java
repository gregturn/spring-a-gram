package com.greglturnquist.springagram;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface ItemRepository extends PagingAndSortingRepository<Item, Long> {

	List<Item> findByGalleryIsNull();

}
