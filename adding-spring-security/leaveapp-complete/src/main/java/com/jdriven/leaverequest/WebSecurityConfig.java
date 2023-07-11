package com.jdriven.leaverequest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@ConditionalOnWebApplication
class WebSecurityConfig {

	private static final MappedJwtClaimSetConverter MAPPED_JWT_CLAIM_CONVERTER = MappedJwtClaimSetConverter.withDefaults(Map.of());

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
			.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
			.oauth2ResourceServer(oauth2 -> oauth2
				.jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakRealmRoleConverter())))
			.build();
	}

	@Bean
	@ConditionalOnProperty(name = "spring.security.oauth2.resourceserver.jwt.issuer-uri")
	JwtDecoder jwtDecoderByIssuerUri(OAuth2ResourceServerProperties properties) {
		final var issuerUri = properties.getJwt().getIssuerUri();
		final var jwtDecoder = JwtDecoders.<NimbusJwtDecoder>fromIssuerLocation(issuerUri);
		jwtDecoder.setClaimSetConverter(userNameSubClaimAdapter());
		return jwtDecoder;
	}

	/**
	 * As per: <a href="https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html#oauth2resourceserver-jwt-authorization-extraction">...</a>
	 */
	@SuppressWarnings("unchecked")
	static Converter<Jwt, JwtAuthenticationToken> keycloakRealmRoleConverter() {
		return jwt -> {
			final var realmAccess = (Map<String, Object>) jwt.getClaims().getOrDefault("realm_access", Map.of());
			final var roles = (List<String>) realmAccess.getOrDefault("roles", List.of());
			final var authorities = roles.stream()
				.map(roleName -> "ROLE_" + roleName)
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());
			return new JwtAuthenticationToken(jwt, authorities);
		};
	}

	/**
	 * Use preferred_username from claims as authentication name, instead of UUID subject.
	 * As per: <a href="https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html#oauth2resourceserver-jwt-claimsetmapping">...</a>
	 *
	 * @return Converter
	 */
	static Converter<Map<String, Object>, Map<String, Object>> userNameSubClaimAdapter() {
		return claims -> {
			final var convertedClaims = MAPPED_JWT_CLAIM_CONVERTER.convert(claims);
			final var username = (String) convertedClaims.get("preferred_username");
			convertedClaims.put("sub", username);
			return convertedClaims;
		};
	}
}
