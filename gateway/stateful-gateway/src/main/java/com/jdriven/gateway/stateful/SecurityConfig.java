package com.jdriven.gateway.stateful;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.session.ReactiveMapSessionRepository;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.web.server.session.SpringSessionWebSessionStore;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

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

	//TODO: investigate usability of existing code in:
	// 	spring security
	//	keycloak spring security plugin

	@Bean
	public ReactiveAuthenticationManager noopAuthManager() {
		//TODO: put tokens on session for later use
		return Mono::just;
	}

	private ServerAuthenticationConverter codeExchanger() {
		//TODO: set-up code for token exchange
		return exchange -> Mono.just(new TestingAuthenticationToken("user", null, "USER"));
	}

	@Bean
	public AuthenticationWebFilter authenticationWebFilter(ReactiveAuthenticationManager authenticationManager) {
		AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(authenticationManager);
		authenticationWebFilter.setRequiresAuthenticationMatcher(pathMatchers(GET, "/exchange")); // trigger on code exchange redirects
		authenticationWebFilter.setSecurityContextRepository(new WebSessionServerSecurityContextRepository());
		authenticationWebFilter.setServerAuthenticationConverter(codeExchanger());
		authenticationWebFilter.setAuthenticationSuccessHandler(new RedirectServerAuthenticationSuccessHandler()); // back to index.html
		return authenticationWebFilter;
	}

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(final AuthenticationWebFilter authenticationWebFilter) {
		// the matcher for all paths that need to be secured (require a logged-in user)
		final ServerWebExchangeMatcher apiPathMatcher = pathMatchers(API_MATCHER_PATH);

		// default chain for all requests
		final ServerHttpSecurity http = this.context.getBean(ServerHttpSecurity.class);

		return http
			.authorizeExchange().matchers(apiPathMatcher).authenticated()
			.anyExchange().permitAll()
			.and().httpBasic().disable()
			.csrf().disable()
			.addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.FORM_LOGIN)

//TODO: setup augmentation of request headers with access_token
// 			.addFilterAt(..., SecurityWebFiltersOrder.AUTHORIZATION)

			.exceptionHandling().authenticationEntryPoint((ex, e) -> Mono.fromRunnable(() -> ex.getResponse().setStatusCode(HttpStatus.FORBIDDEN))).and()
			.logout().logoutSuccessHandler((ex, auth) -> Mono.just(ex.getExchange().getResponse().setStatusCode(HttpStatus.OK)).then())
			.and().build();
	}
}