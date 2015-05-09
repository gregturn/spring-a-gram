package com.greglturnquist.springagram;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

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

	public long getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}
}
