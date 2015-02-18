package com.greglturnquist.springagram;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.hateoas.Link;

@Entity
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Lob
	private String image;

	@ManyToOne
	private Gallery gallery;

	@JsonIgnore
	@OneToOne
	private User user;

	private Link htmlUrl;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public Gallery getGallery() {
		return gallery;
	}

	public void setGallery(Gallery gallery) {
		this.gallery = gallery;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Link getHtmlUrl() {
		if (htmlUrl == null) {
			htmlUrl = linkTo(methodOn(ApplicationController.class).image(this.id)).withRel("htmlUrl");
		}
		return htmlUrl;
	}

}
