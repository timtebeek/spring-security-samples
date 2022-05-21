package com.jdriven;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

	public static final String REALM_ACCESS = "realm_access";
	public static final String ROLES = "roles";
	public static final String ROLE_PREFIX = "ROLE_";

	@SuppressWarnings("unchecked")
	@Override
	public Collection<GrantedAuthority> convert(final Jwt jwt) {
		final Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get(REALM_ACCESS);
		return ((List<String>)realmAccess.get(ROLES)).stream()
			.map(roleName -> ROLE_PREFIX + roleName)
			.map(SimpleGrantedAuthority::new)
			.collect(Collectors.toList());
	}
}
