package com.greglturnquist.springagram.frontend;

import lombok.Data;

@Data
public class Item {

	private String image;
	private Gallery gallery;
	private User user;

}
