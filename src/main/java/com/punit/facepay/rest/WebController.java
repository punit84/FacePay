package com.punit.facepay.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebController {

	@RequestMapping(value = "/index")
	public String index() {
		return "index";
	}
	
	@RequestMapping(value = "/test")
	public String test() {
		return "test";
	}
	
	@RequestMapping(value = "/home")
	public String home() {
		return "index";
	}

	@RequestMapping(value = "/admin")
	public String admin() {
		return "admin";
	}

	@RequestMapping(value = "/qart")
	public String qart() {
		return "qart";
	}
	
	@RequestMapping(value = "/contact")
	public String contact() {
		return "contact";
	}
	
	@RequestMapping(value = "/about")
	public String about() {
		return "/about";
	}

	@RequestMapping(value = "/e")
	public String error() {
		return "error";
	}
	
	@RequestMapping(value = "/p")
	public String profile() {
		return "profile";
	}
	
}