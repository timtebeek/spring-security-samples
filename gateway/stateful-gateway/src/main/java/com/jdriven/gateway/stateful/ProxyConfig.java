package com.jdriven.gateway.stateful;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Consumer;

import static org.springframework.web.reactive.function.BodyExtractors.toDataBuffers;
import static org.springframework.web.reactive.function.BodyInserters.fromDataBuffers;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;


@Configuration
public class ProxyConfig {

	private static final String API_PREFIX = "/api/";
	private static final String CATCH_ALL_SUFFIX = "/**";

	private final ReactiveOAuth2AuthorizedClientService authorizedClientService;
	private final GatewayConfig config;

	public ProxyConfig(final ReactiveOAuth2AuthorizedClientService authorizedClientService, final GatewayConfig config) {
		this.authorizedClientService = authorizedClientService;
		this.config = config;
	}

	@Bean
	public RouterFunction<ServerResponse> index(@Value("classpath:/public/index.html") final Resource index) {
		return route(GET("/"), request -> ok().contentType(MediaType.TEXT_HTML).syncBody(index));
	}

	@Bean
	public RouterFunction<ServerResponse> proxy(WebClient webClient) {
		return route(GET(API_PREFIX + "{service}" + CATCH_ALL_SUFFIX), request -> authorizedClient(request)
			.map(attr -> webClient.method(request.method())
				.uri(toBackendUri(request))
				.body(fromDataBuffers(request.exchange().getRequest().getBody()))
				.attributes(attr))
			.flatMap(WebClient.RequestHeadersSpec::exchange)
			.flatMap(response -> ServerResponse.status(response.statusCode())
				.headers(headers -> headers.putAll(response.headers().asHttpHeaders()))
				.body(fromDataBuffers(response.body(toDataBuffers())))));
	}

	private String toBackendUri(final ServerRequest request) {
		final String service = request.pathVariable("service");
		final String backend = config.backend(service);
		return UriComponentsBuilder.fromUriString(backend)
			.path(request.path().substring(API_PREFIX.length() + service.length()))
			.queryParams(request.queryParams())
			.build().toString();
	}

	private Mono<Consumer<Map<String, Object>>> authorizedClient(final ServerRequest request) {
		return request.principal()
			.cast(OAuth2AuthenticationToken.class)
			.flatMap(this::loadAuthorizedClient)
			.map(ServerOAuth2AuthorizedClientExchangeFilterFunction::oauth2AuthorizedClient);
	}

	private Mono<OAuth2AuthorizedClient> loadAuthorizedClient(OAuth2AuthenticationToken authentication) {
		return authorizedClientService.loadAuthorizedClient(authentication.getAuthorizedClientRegistrationId(), authentication.getName());
	}
}
