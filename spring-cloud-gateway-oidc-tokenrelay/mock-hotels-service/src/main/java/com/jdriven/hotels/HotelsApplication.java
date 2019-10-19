package com.jdriven.hotels;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@SpringBootApplication
@Controller
public class HotelsApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(HotelsApplication.class, args);
	}

	@GetMapping("/")
	public String getIndex() {
		return "index";
	}

	@GetMapping("/whoami")
	@ResponseBody
	public Jwt index(@AuthenticationPrincipal Jwt jwt) {
		return jwt;
	}

}
