package com.jdriven.gateway.stateful;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

// not an @Component to prevent spring from automatically adding it for any request
public class TokenAugmentingFilter implements WebFilter {
	private final ServerWebExchangeMatcher apiMatcher;
	private final ReactiveOAuth2AuthorizedClientService authorizedClientService;

	TokenAugmentingFilter(final ServerWebExchangeMatcher apiMatcher, final ReactiveOAuth2AuthorizedClientService authorizedClientService) {
		this.apiMatcher = apiMatcher;
		this.authorizedClientService = authorizedClientService;
	}

	@Override
	public Mono<Void> filter(final ServerWebExchange exchange, final WebFilterChain chain) {
		return apiMatcher.matches(exchange)
			.filter(ServerWebExchangeMatcher.MatchResult::isMatch)
			.flatMap(match -> exchange.getPrincipal())
			.cast(OAuth2AuthenticationToken.class)
			.flatMap(authentication -> authorizedClientService.loadAuthorizedClient(
				authentication.getAuthorizedClientRegistrationId(), authentication.getName())
				.map(authorizedClient -> {
					final ServerHttpRequest request = exchange.getRequest();
					return exchange.mutate().request(request.mutate()
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + authorizedClient.getAccessToken().getTokenValue())
						.build()).build();
				}))
			.switchIfEmpty(Mono.just(exchange))
			.flatMap(chain::filter);
	}
}
