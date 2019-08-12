package com.jdriven.gateway.stateful.filter;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

public class ClientProvidingWebClientFilter extends ModifiedWebClientHttpRoutingFilter {
	private final ReactiveOAuth2AuthorizedClientService authorizedClientService;

	public ClientProvidingWebClientFilter(final WebClient webClient, final ObjectProvider<List<HttpHeadersFilter>> headersFiltersProvider,
										  final ReactiveOAuth2AuthorizedClientService authorizedClientService) {
		super(webClient, headersFiltersProvider);
		this.authorizedClientService = authorizedClientService;
	}

	@Override
	protected Mono<RequestHeadersSpec<?>> augmentWebClientSpec(final RequestHeadersSpec<?> headersSpec, final ServerWebExchange exchange) {
		return exchange.getPrincipal()
			.cast(OAuth2AuthenticationToken.class)
			.flatMap(this::loadAuthorizedClient)
			.map(ServerOAuth2AuthorizedClientExchangeFilterFunction::oauth2AuthorizedClient)
			.map(headersSpec::attributes);
	}

	private Mono<OAuth2AuthorizedClient> loadAuthorizedClient(OAuth2AuthenticationToken authentication) {
		return authorizedClientService.loadAuthorizedClient(authentication.getAuthorizedClientRegistrationId(), authentication.getName());
	}
}
