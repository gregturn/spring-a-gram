package com.greglturnquist.springagram;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface GalleryRepository extends PagingAndSortingRepository<Gallery, Long> {
}
