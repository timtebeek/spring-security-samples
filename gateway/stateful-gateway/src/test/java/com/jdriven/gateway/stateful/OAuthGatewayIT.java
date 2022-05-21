package com.jdriven.gateway.stateful;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@SpringBootTest(webEnvironment = DEFINED_PORT)
class OAuthGatewayIT {

	private static final MockWebServer service1Mock = new MockWebServer();
	private static final MockWebServer idpMock = new MockWebServer();
	private static final String SESSION = "SESSION";

	@BeforeAll
	static void startMocks() throws IOException {
		service1Mock.start(9081);
		idpMock.start(9180);
	}

	@AfterAll
	static void tearDown() throws IOException {
		service1Mock.shutdown();
		idpMock.shutdown();
	}

	@Test
	void serveIndex() {
		final String indexContent = WebClient.create("http://localhost:9080")
				.get()
				.retrieve()
				.bodyToMono(String.class)
				.block();

		assertEquals("<!DOCTYPE html>", indexContent.substring(0, 15));
	}

	@Test
	void redirectCallsWithoutValidCookie() {
		final ClientResponse initialResponse = WebClient.create("http://localhost:9080")
				.get()
				.uri("/api/service1/hello")
				.exchange().block();

		assertNotNull(initialResponse);
		assertEquals(HttpStatus.FOUND, initialResponse.statusCode());
		assertNotNull(initialResponse.headers().asHttpHeaders().getLocation());
		assertFalse(initialResponse.cookies().containsKey(SESSION));
		assertEquals("/oauth2/authorization/dummy-idp",
				initialResponse.headers().asHttpHeaders().getLocation().toString());
	}

	@Test
	void oauthEndpointRedirectsToIdP() {
		final ClientResponse initialResponse = WebClient.create("http://localhost:9080")
				.get()
				.uri("/oauth2/authorization/dummy-idp")
				.exchange().block();

		assertNotNull(initialResponse);
		assertEquals(HttpStatus.FOUND, initialResponse.statusCode());
		assertNotNull(initialResponse.headers().asHttpHeaders().getLocation());
		assertTrue(initialResponse.cookies().containsKey(SESSION));

		final URI idpLocation = initialResponse.headers().asHttpHeaders().getLocation();
		assertNotNull(idpLocation);
		assertTrue(idpLocation.isAbsolute());
		assertEquals("/dummy-idp-auth", idpLocation.getPath());
		assertTrue(idpLocation.getQuery().contains("response_type=code&client_id=gateway&scope=profile&state="));

		// redirect to IdP for login, should also contain the redirect_uri param to redirect back to after successful login
		assertTrue(idpLocation.getQuery().contains("&redirect_uri=http://localhost:9080/login/oauth2/code/dummy-idp"));
	}

	@Test
	void testCompleteLoginAndBackendCallFlow() throws JsonProcessingException, InterruptedException {
		// step 1: request authZ
		final ClientResponse initialResponse = WebClient.create("http://localhost:9080")
				.get()
				.uri("/oauth2/authorization/dummy-idp")
				.exchange().block();

		// save cookie for use in later calls
		ResponseCookie sessionCookie = initialResponse.cookies().getFirst(SESSION);

		// pretend we logged in at the IdP
		final String query = initialResponse.headers().asHttpHeaders().getLocation().getQuery();
		final String state = query.substring(query.indexOf("state="), query.indexOf("&redirect"));
		String dummyRedirect = "http://localhost:9080/login/oauth2/code/dummy-idp?code=dummy-code&" + state;

		// play IdP and accept any authorization code check call and just give an access token back
		final HashMap<String, Object> tokenResponse = new HashMap<>();
		tokenResponse.put("access_token", "dummy-token");
		tokenResponse.put("token_type", "bearer");
		tokenResponse.put("expires_in", 300);

		idpMock.enqueue(
				new MockResponse()
						.setResponseCode(200)
						.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.setBody(new ObjectMapper().writeValueAsString(tokenResponse)));

		// play IdP and provide back user details which are requested directly after the token
		idpMock.enqueue(
				new MockResponse()
						.setResponseCode(200)
						.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.setBody(new ObjectMapper().writeValueAsString(Collections.singletonMap("username", "Dummy"))));

		// step 2: trigger the server-side code to token exchange call to our idpMock
		final ClientResponse authResponse = WebClient.create(dummyRedirect)
				.get()
				.cookie(SESSION, sessionCookie.getValue())
				.exchange().block();

		// check that client is now successfully logged in and are now redirected back to "/"
		assertEquals(HttpStatus.FOUND, authResponse.statusCode());
		assertEquals("/", authResponse.headers().asHttpHeaders().getLocation().toString());

		// update the cookie with the new value from the received Set-Cookie header
		sessionCookie = authResponse.cookies().getFirst(SESSION);

		// prepare service backend
		service1Mock.enqueue(
				new MockResponse()
						.setResponseCode(200)
						.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.setBody(new ObjectMapper()
								.writeValueAsString(Collections.singletonMap("message", "hello mocked user"))));

		// step 3: call actual service with the session cookie
		final ClientResponse apiResponse = WebClient.create("http://localhost:9080")
				.get()
				.uri("/api/service1/hello")
				.cookie(SESSION, sessionCookie.getValue())
				.exchange().block();

		assertEquals(HttpStatus.OK, apiResponse.statusCode());
		//final String result = apiResponse.bodyToMono(String.class).block();
		final Map jsonMap = apiResponse.bodyToMono(Map.class).block();
		assertNotNull(jsonMap);
		assertEquals("hello mocked user", jsonMap.get("message"));

		// check if the token was send to our backend service
		RecordedRequest recordedRequest = service1Mock.takeRequest();
		assertEquals("Bearer dummy-token", recordedRequest.getHeader("Authorization"));
	}
}
