package com.jdriven.gateway.stateful;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

@EnableWebFluxSecurity
@Configuration
public class SecurityConfig {

	public static final String API_MATCHER_PATH = "/api/**";
	private final ApplicationContext context;

	public SecurityConfig(final ApplicationContext context) {
		this.context = context;
	}

	@Bean
	WebClient tokenAugmentingWebClient(
			final ReactiveClientRegistrationRepository clientRegistrationRepository,
			final ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
		return WebClient.builder()
			.filter(new ServerOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrationRepository, authorizedClientRepository))
			.build();
	}

	@Bean
	SecurityWebFilterChain securityWebFilterChain() {
		// the matcher for all paths that need to be secured (require a logged-in user)
		final ServerWebExchangeMatcher apiPathMatcher = pathMatchers(API_MATCHER_PATH);

		// default chain for all requests
		final ServerHttpSecurity http = this.context.getBean(ServerHttpSecurity.class);

		return http
				.authorizeExchange().matchers(apiPathMatcher).authenticated()
				.anyExchange().permitAll()
				.and().httpBasic().disable()
				.csrf().disable()
				.oauth2Client()
				.and()
				.oauth2Login()
				.and()
				.build();
	}
}
