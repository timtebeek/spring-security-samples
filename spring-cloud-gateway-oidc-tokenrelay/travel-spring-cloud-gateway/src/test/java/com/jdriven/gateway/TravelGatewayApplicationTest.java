package com.jdriven.gateway;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClientConfigurer;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockAuthentication;

@SpringBootTest
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8090)
class TravelGatewayApplicationTest {
	@Autowired
	private WebTestClient client;

	@Test
	void testGetIndexAnonymously() throws Exception {
		client.get().uri("/").exchange()
				.expectStatus().is3xxRedirection()
				.expectHeader().value(HttpHeaders.LOCATION, is("/oauth2/authorization/keycloak"));
	}

	@Test
	void testGetIndexAuthenticated() throws Exception {
		client.mutateWith(oauth2Authentication())
				.get().uri("/").exchange()
				.expectStatus().is2xxSuccessful()
				.expectBody(String.class).value(containsString("Logout <span>Subject A</span>"));
	}

	@Test
	void testGetHomeAuthenticated() throws Exception {
		client.mutateWith(oauth2Authentication())
				.get().uri("/home").exchange()
				.expectStatus().is2xxSuccessful()
				.expectBody(String.class).value(containsString("href=\"/whoami\"><span>Subject A</span></a>."));
	}

	private static WebTestClientConfigurer oauth2Authentication() {
		Map<String, Object> claims = Map.of("sub", "Subject A");
		OidcIdToken token = new OidcIdToken("jwt", Instant.now(), Instant.MAX, claims);
		Collection<? extends GrantedAuthority> auths = List.of(new SimpleGrantedAuthority("user"));
		OAuth2User principal = new DefaultOidcUser(auths, token);
		Authentication auth = new OAuth2AuthenticationToken(principal, auths, "keycloak");
		return mockAuthentication(auth);
	}

}
