package com.jdriven.gateway.stateful;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class TestController {

	@GetMapping("/token")
	Mono<OAuth2AuthenticationToken> token(final OAuth2AuthenticationToken token) {
		return Mono.just(token);
	}

	@GetMapping("/principal")
	Mono<OAuth2User> principal(@AuthenticationPrincipal OAuth2User principal) {
		return Mono.just(principal);
	}
}
