package com.jdriven.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOidcLogin;

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
		client.mutateWith(mockOidcLogin().idToken(builder -> builder.subject("Subject A")))
				.get().uri("/").exchange()
				.expectStatus().is2xxSuccessful()
				.expectBody(String.class).value(containsString("Logout <span>Subject A</span>"));
	}

	@Test
	void testGetHomeAuthenticated() throws Exception {
		client.mutateWith(mockOidcLogin().idToken(builder -> builder.subject("Subject A")))
				.get().uri("/home").exchange()
				.expectStatus().is2xxSuccessful()
				.expectBody(String.class).value(containsString("href=\"/whoami\"><span>Subject A</span></a>."));
	}

}
