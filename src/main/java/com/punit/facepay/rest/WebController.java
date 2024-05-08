package com.punit.facepay.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebController {

	@RequestMapping(value = "/index")
	public String index() {
		return "index";
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
	
	@RequestMapping(value = "/login")
	public String login() {
		return "/login";
	}
	@RequestMapping(value = "/1")
	public String index2() {
		return "1";
	}

	@RequestMapping(value = "/e")
	public String error() {
		return "error";
	}
	
	@RequestMapping(value = "/NOTFOUND")
	public String NOTFOUND() {
		return "NOTFOUND";
	}
	
	
	@RequestMapping(value = "/p")
	public String profile() {
		return "profile";
	}
	
	@RequestMapping(value = "/userinfo")
	public String userinfo() {
		return "userinfo";
	}
	
}