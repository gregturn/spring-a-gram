package com.greglturnquist.springagram.backend;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import lombok.Data;

@Data
@Entity
public class Gallery {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String description;

	// tag::items-def[]
	@OneToMany(mappedBy = "gallery")
	private List<Item> items;
	// end::items-def[]

	protected Gallery() {}

	public Gallery(String description) {
		this.description = description;
	}

}
