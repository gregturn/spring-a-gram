package com.greglturnquist.springagram.backend;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface GalleryRepository extends PagingAndSortingRepository<Gallery, Long> {
}
