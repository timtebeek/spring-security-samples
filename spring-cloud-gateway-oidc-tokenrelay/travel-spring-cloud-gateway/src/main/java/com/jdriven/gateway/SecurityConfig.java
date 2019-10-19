package com.jdriven.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter.Mode;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) throws Exception {
		// Disable Gateway CSRF to prevent conflicts with proxied service CSRF
		http.csrf().disable();
		http.headers().frameOptions().mode(Mode.SAMEORIGIN);
		http.authorizeExchange()
				.anyExchange().authenticated()
				.and()
				.oauth2Login();
		return http.build();
	}

}
