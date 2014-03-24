package com.greglturnquist.springagram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

	@Autowired
	ItemRepository repository;

	@RequestMapping("/home")
	public String home(Model model) {

		model.addAttribute("items", repository.findAll());
		return "home";
	}
}
