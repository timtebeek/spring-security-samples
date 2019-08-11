package com.jdriven.gateway.stateful;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.session.ReactiveMapSessionRepository;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.web.server.session.SpringSessionWebSessionStore;

import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

@EnableWebFluxSecurity
@Configuration
public class SecurityConfig {

	private static final String API_MATCHER_PATH = "/api/**";
	private final ApplicationContext context;

	public SecurityConfig(final ApplicationContext context) {
		this.context = context;
	}

	@Bean
	public SpringSessionWebSessionStore<? extends Session> sessionStore(ReactiveSessionRepository<? extends Session> repository) {
		return new SpringSessionWebSessionStore<>(repository);
	}

	@Bean
	public ReactiveSessionRepository sessionRepository() {
		return new ReactiveMapSessionRepository(new ConcurrentHashMap<>());
	}

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(final ReactiveOAuth2AuthorizedClientService reactiveOAuth2AuthorizedClientService) {
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

 			.addFilterAt(new TokenAugmentingFilter(apiPathMatcher, reactiveOAuth2AuthorizedClientService), SecurityWebFiltersOrder.AUTHORIZATION)

			.build();
	}
}