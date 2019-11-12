package com.jdriven.leaverequest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static com.jdriven.leaverequest.LeaveRequest.Status.APPROVED;
import static com.jdriven.leaverequest.LeaveRequest.Status.DENIED;
import static com.jdriven.leaverequest.LeaveRequest.Status.PENDING;
import static java.time.LocalDate.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 8090)
class LeaveRequestControllerSpringBootWebEnvRandomPortTest {

	private static final String TOKEN_ALICE_VALID_UNTIL_AUGUST_2022 = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJRSS1mdW1sLTRyTnBrdVZxWVg0elpuZlRUZW1hSkxoZ183Z0dULTZiSFlVIn0.eyJqdGkiOiI0OWFkZmVlOS02MGEwLTQxMTItOGI1OS1hZWEwOTliYjlmYTIiLCJleHAiOjE2NTk4OTg3NjYsIm5iZiI6MCwiaWF0IjoxNTczNDk4NzY2LCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwOTAvYXV0aC9yZWFsbXMvc3ByaW5nLWNsb3VkLWdhdGV3YXktcmVhbG0iLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiYWQ4MjRkZmMtNmFmZi00NWEzLWJlOWQtOTU0ZDUwZmJkYjdhIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoic3ByaW5nLWNsb3VkLWdhdGV3YXktY2xpZW50Iiwibm9uY2UiOiJwMGtsejZlc0VYNnYtWU8tXzVGd2JITk95N2NudklibEsweTBiWi1iUVpNIiwiYXV0aF90aW1lIjoxNTczNDk4NzY2LCJzZXNzaW9uX3N0YXRlIjoiZjIyMjY4ZTAtOTJlZS00MDU4LTgzMzctMzkyNDg4YjJlNTQwIiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJvcGVuaWQgcGhvbmUgbWljcm9wcm9maWxlLWp3dCBvZmZsaW5lX2FjY2VzcyBwcm9maWxlIGVtYWlsIGFkZHJlc3MiLCJ1cG4iOiJhbGljZSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJhZGRyZXNzIjp7fSwibmFtZSI6IkFsaWNlIiwiZ3JvdXBzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXSwicHJlZmVycmVkX3VzZXJuYW1lIjoiYWxpY2UiLCJnaXZlbl9uYW1lIjoiQWxpY2UifQ.hHK8E-01eFb4UZtBjclVronp-6jfVbhxh9U0m0DEdYQE8YGWFCjMxUcfeI0Rx9zueeC_HJsFT4wcegs8Qh6Z2n7IGWbf0Vtg0fzVH9vFXkwbFBdbCZJz4MZ1Zc3cpC2UrSnuPDc8EIn1FQrKIEF8nqEcNzcL3ujxligvOern2A4xUS1GLqrLKmbPPbKocESZb75EMbmEPFDoctgI1n4vniOz9j242WxbcMjM7INeIsPCKWqvgaeYHOKlgW2CfHfsLPr_XQw5VvCMWJ1GHficCwwjyL-PeemHmBS6eO6EnHpl5Ftm7siATcZuJXQHTosb6b38Gkw2s6-yGQJcbQc01g";
	private static final String TOKEN_HR_VALID_UNTIL_AUGUST_2022 = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJRSS1mdW1sLTRyTnBrdVZxWVg0elpuZlRUZW1hSkxoZ183Z0dULTZiSFlVIn0.eyJqdGkiOiJhZjJkYjUzZi0zMzA3LTRiYmItOWIyMy01NjI4NzYxOTNiOTAiLCJleHAiOjE2NTk4OTgyMjIsIm5iZiI6MCwiaWF0IjoxNTczNDk4MjIyLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwOTAvYXV0aC9yZWFsbXMvc3ByaW5nLWNsb3VkLWdhdGV3YXktcmVhbG0iLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiYjc2ODI2NjQtZWJhZC00NzAzLTgyNTctNDIyY2QwNTJhNjU4IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoic3ByaW5nLWNsb3VkLWdhdGV3YXktY2xpZW50Iiwibm9uY2UiOiJDMFktek5kTUt6UWwweGdESDc1OWZJOU13ek5iY3ROT3I2M0ZOMUFSX1NzIiwiYXV0aF90aW1lIjoxNTczNDk4MjIyLCJzZXNzaW9uX3N0YXRlIjoiODA5NGMxZGUtMTNmYS00MzQzLWEyN2MtODRhZTE3NDJlMDA5IiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsIkhSIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6Im9wZW5pZCBwaG9uZSBtaWNyb3Byb2ZpbGUtand0IG9mZmxpbmVfYWNjZXNzIHByb2ZpbGUgZW1haWwgYWRkcmVzcyIsInVwbiI6ImJvYiIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJhZGRyZXNzIjp7fSwibmFtZSI6IkJvYiIsImdyb3VwcyI6WyJvZmZsaW5lX2FjY2VzcyIsIkhSIiwidW1hX2F1dGhvcml6YXRpb24iXSwicHJlZmVycmVkX3VzZXJuYW1lIjoiYm9iIiwiZ2l2ZW5fbmFtZSI6IkJvYiJ9.DxqIEqanm_7pM08amW6VQ7EBXta54c7kV2ozewYZ_K6_nqOZHAQMWiH-DTnCLlNPnoKZeoLsf_7QcRAXijGoCiRYy0suhGvLAAMCtQgDMfmgMYCZV-sOkHNX8AZDzDj3tXQs1zVQOc3zw9M3Byt_e-0KzxcIi4EUcD8p5fi7NTsynV7Lm6KQVMQTVE-eWBN9wtNo6Lacv4WeFcOTYTRTVRHJU72rrXe6hSDpRd90__T2PsncnqvKPhtaSe4klgaENflxQRTEVKu_oDah4MabwFW1yqQRX68OvUdmtOI2sd05p1juYwuIbSThGtcgrqA1VomgHu0fbp6bgvd1Q_neuw";

	@Autowired
	private LeaveRequestRepository repository;

	@Autowired
	private TestRestTemplate resttemplate;

	@BeforeEach
	void beforeEach() {
		repository.clear();
	}

	@Test
	void testRequest() throws Exception {
		LocalDate from = of(2019, 11, 30);
		LocalDate to = of(2019, 12, 3);

		HttpEntity<?> httpEntity = httpEntityWithBearerTokenHeader(TOKEN_ALICE_VALID_UNTIL_AUGUST_2022);
		ResponseEntity<LeaveRequestDTO> response = resttemplate.exchange("/request/{employee}?from={from}&to={to}",
				HttpMethod.POST, httpEntity, LeaveRequestDTO.class, "alice", from, to);
		assertThat(response.getStatusCode()).isEqualByComparingTo(ACCEPTED);
		assertThat(response.getHeaders().getContentType()).isEqualByComparingTo(APPLICATION_JSON);
		assertThat(response.getBody().getEmployee()).isEqualTo("alice");
		assertThat(response.getBody().getFromDate()).isEqualTo(from);
		assertThat(response.getBody().getToDate()).isEqualTo(to);
		assertThat(response.getBody().getStatus()).isEqualByComparingTo(PENDING);
	}

	@Test
	void testApprove() throws Exception {
		LeaveRequest saved = repository.save(new LeaveRequest("Alice", of(2019, 11, 30), of(2019, 12, 3), PENDING));
		HttpEntity<?> httpEntity = httpEntityWithBearerTokenHeader(TOKEN_HR_VALID_UNTIL_AUGUST_2022);
		ResponseEntity<LeaveRequestDTO> response = resttemplate.exchange("/approve/{id}", HttpMethod.POST, httpEntity,
				LeaveRequestDTO.class, saved.getId());
		assertThat(response.getStatusCode()).isEqualByComparingTo(ACCEPTED);
		assertThat(response.getHeaders().getContentType()).isEqualByComparingTo(APPLICATION_JSON);
		assertThat(response.getBody().getEmployee()).isEqualTo("Alice");
		assertThat(response.getBody().getStatus()).isEqualByComparingTo(APPROVED);
	}

	@Test
	void testApproveMissing() throws Exception {
		HttpEntity<?> httpEntity = httpEntityWithBearerTokenHeader(TOKEN_HR_VALID_UNTIL_AUGUST_2022);
		ResponseEntity<LeaveRequestDTO> response = resttemplate.exchange("/approve/{id}", HttpMethod.POST, httpEntity,
				LeaveRequestDTO.class, UUID.randomUUID());
		assertThat(response.getStatusCode()).isEqualByComparingTo(NO_CONTENT);
	}

	@Test
	void testDeny() throws Exception {
		LeaveRequest saved = repository.save(new LeaveRequest("Alice", of(2019, 11, 30), of(2019, 12, 3), PENDING));
		HttpEntity<?> httpEntity = httpEntityWithBearerTokenHeader(TOKEN_HR_VALID_UNTIL_AUGUST_2022);
		ResponseEntity<LeaveRequestDTO> response = resttemplate.exchange("/deny/{id}", HttpMethod.POST, httpEntity,
				LeaveRequestDTO.class, saved.getId());
		assertThat(response.getStatusCode()).isEqualByComparingTo(ACCEPTED);
		assertThat(response.getHeaders().getContentType()).isEqualByComparingTo(APPLICATION_JSON);
		assertThat(response.getBody().getEmployee()).isEqualTo("Alice");
		assertThat(response.getBody().getStatus()).isEqualByComparingTo(DENIED);
	}

	@Test
	void testViewId() throws Exception {
		LeaveRequest saved = repository.save(new LeaveRequest("alice", of(2019, 11, 30), of(2019, 12, 3), PENDING));
		HttpEntity<?> httpEntity = httpEntityWithBearerTokenHeader(TOKEN_ALICE_VALID_UNTIL_AUGUST_2022);
		ResponseEntity<LeaveRequestDTO> response = resttemplate.exchange("/view/id/{id}", GET, httpEntity,
				LeaveRequestDTO.class, saved.getId());
		assertThat(response.getStatusCode()).isEqualByComparingTo(OK);
		assertThat(response.getHeaders().getContentType()).isEqualByComparingTo(APPLICATION_JSON);
		assertThat(response.getBody().getEmployee()).isEqualTo("alice");
		assertThat(response.getBody().getStatus()).isEqualByComparingTo(PENDING);
	}

	@Test
	void testViewIdMissing() throws Exception {
		HttpEntity<?> httpEntity = httpEntityWithBearerTokenHeader(TOKEN_HR_VALID_UNTIL_AUGUST_2022);
		ResponseEntity<LeaveRequestDTO> response = resttemplate.exchange("/view/id/{id}", GET, httpEntity,
				LeaveRequestDTO.class, UUID.randomUUID());
		assertThat(response.getStatusCode()).isEqualByComparingTo(NO_CONTENT);
	}

	private static final ParameterizedTypeReference<List<LeaveRequestDTO>> typeref = new ParameterizedTypeReference<>() {
	};

	@Test
	void testViewEmployee() throws Exception {
		repository.save(new LeaveRequest("alice", of(2019, 11, 30), of(2019, 12, 3), PENDING));
		HttpEntity<?> httpEntity = httpEntityWithBearerTokenHeader(TOKEN_ALICE_VALID_UNTIL_AUGUST_2022);
		ResponseEntity<List<LeaveRequestDTO>> response = resttemplate.exchange("/view/employee/{employee}", GET,
				httpEntity, typeref, "alice");
		assertThat(response.getStatusCode()).isEqualByComparingTo(OK);
		assertThat(response.getHeaders().getContentType()).isEqualByComparingTo(APPLICATION_JSON);
		assertThat(response.getBody().get(0).getEmployee()).isEqualTo("alice");
		assertThat(response.getBody().get(0).getStatus()).isEqualByComparingTo(PENDING);
	}

	@Test
	void testViewAll() throws Exception {
		repository.save(new LeaveRequest("Alice", of(2019, 11, 30), of(2019, 12, 3), PENDING));
		HttpEntity<?> httpEntity = httpEntityWithBearerTokenHeader(TOKEN_HR_VALID_UNTIL_AUGUST_2022);
		ResponseEntity<List<LeaveRequestDTO>> response = resttemplate.exchange("/view/all", GET,
				httpEntity, typeref);
		assertThat(response.getStatusCode()).isEqualByComparingTo(OK);
		assertThat(response.getHeaders().getContentType()).isEqualByComparingTo(APPLICATION_JSON);
		assertThat(response.getBody().get(0).getEmployee()).isEqualTo("Alice");
		assertThat(response.getBody().get(0).getStatus()).isEqualByComparingTo(PENDING);
	}

	private static HttpEntity<?> httpEntityWithBearerTokenHeader(String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token);
		HttpEntity<?> httpEntity = new HttpEntity<>(headers);
		return httpEntity;
	}

}
