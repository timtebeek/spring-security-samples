package com.jdriven;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

	@GetMapping("/hello")
	public Map<String, String> hello(final @AuthenticationPrincipal Jwt jwt) {
		System.out.println("claims:\n" + jwt.getClaims());
		System.out.println("\nheaders:\n" + jwt.getHeaders());
		return Map.of("message", "Hello " + jwt.getClaimAsString("name"));
	}
}
