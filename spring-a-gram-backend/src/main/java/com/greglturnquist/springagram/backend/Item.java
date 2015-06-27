package com.greglturnquist.springagram.backend;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Lob
	private String image;

	// tag::gallery-def[]
	@ManyToOne
	private Gallery gallery;
	// end::gallery-def[]

	// tag::user-def[]
	@JsonIgnore
	@OneToOne
	private User user;
	// end::user-def[]

	/**
	 * TODO: Lombok generated some error inside IntelliJ. Only solution was to hand write this setter.
	 * @param user
	 */
	public void setUser(User user) {
		this.user = user;
	}

}
