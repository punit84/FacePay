package com.punit.facepay.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebController {

	@RequestMapping(value = "/index")
	public String index() {
		return "index";
	}

	@RequestMapping(value = "/a")
	public String admin() {
		return "admin";
	}

	@RequestMapping(value = "/error")
	public String error() {
		return "error";
	}
}