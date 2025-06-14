package com.punit.AWSPe.rest;

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
	public String p() {
		return "profile";
	}

	@RequestMapping(value = "/profile")
	public String profile() {
		return "profile";
	}
	@RequestMapping(value = "/face")
	public String face() {
		return "face";
	}

	@RequestMapping(value = "/document")
	public String document() {
		return "document";
	}

	@RequestMapping(value = "/kyc")
	public String kyc() {
		return "kyc";
	}

	@RequestMapping(value = "/kycReko")
	public String kycReko() {
		return "kycReko";
	}

	@RequestMapping(value = "/support")
	public String support() {
		return "support";
	}

	@RequestMapping(value = "/voice")
	public String voice() {
		return "voiceChat";
	}

}