package com.jdriven.flights;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@SpringBootApplication
@Controller
public class FlightsApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlightsApplication.class, args);
	}

	@GetMapping("/")
	public String getIndex() {
		return "index";
	}

	@GetMapping("/whoami")
	@ResponseBody
	public Authentication whoami(Authentication auth) {
		return auth;
	}

}
