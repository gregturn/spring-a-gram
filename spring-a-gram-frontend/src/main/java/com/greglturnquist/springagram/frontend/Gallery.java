package com.greglturnquist.springagram.frontend;

import java.util.List;

import lombok.Data;

@Data
public class Gallery {

	private String description;
	private List<Item> items;

}
