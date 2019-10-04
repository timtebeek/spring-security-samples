package com.jdriven;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
public class HelloController {

	@GetMapping("/hello")
	public Map<String, String> hello(final @AuthenticationPrincipal Jwt jwt) {
		System.out.println("Reached 'hello' endpoint with jwt claims: " + jwt.getClaims());
		return Collections.singletonMap("message", "Hello " + jwt.getClaimAsString("name"));
	}
}
