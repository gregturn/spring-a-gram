package com.greglturnquist.springagram;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

import org.springframework.hateoas.Link;
import org.springframework.ui.Model;

@Entity
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String name;

	@Lob
	private String image;

	@ManyToOne
	private Gallery gallery;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public Link getHtmlUrl() throws NoSuchMethodException {
		Method method = ApplicationController.class.getMethod("image", Long.class, Model.class, HttpServletRequest.class);
		return linkTo(ApplicationController.class, method, id).withRel("htmlUrl");
	}

}
