package com.greglturnquist.springagram.backend;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;

// tag::top-level[]
@PreAuthorize("hasRole('ROLE_USER')")
public interface ItemRepository extends PagingAndSortingRepository<Item, Long> {
// end::top-level[]

	List<Item> findByGalleryIsNull();

	// tag::save-item[]
	@Override
	@PreAuthorize("#item?.user == null or #item?.user?.name == authentication?.name")
	Item save(@Param("item") Item item);
	// end::save-item[]

	@Override
	@PreAuthorize("#item?.user?.name == authentication?.name")
	void delete(@Param("item") Item item);

	// tag::delete[]
	@Override
	@PreAuthorize("@itemRepository.findOne(#id)?.user?.name == authentication?.name")
	void delete(@Param("id") Long id);
	// end::delete[]
}
