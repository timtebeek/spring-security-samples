package com.jdriven.leaverequest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
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
		ResponseEntity<LeaveRequestDTO> response = resttemplate.postForEntity("/request/{employee}?from={from}&to={to}",
				null, LeaveRequestDTO.class, "Alice", from, to);
		assertThat(response.getStatusCode()).isEqualByComparingTo(ACCEPTED);
		assertThat(response.getHeaders().getContentType()).isEqualByComparingTo(APPLICATION_JSON);
		assertThat(response.getBody().getEmployee()).isEqualTo("Alice");
		assertThat(response.getBody().getFromDate()).isEqualTo(from);
		assertThat(response.getBody().getToDate()).isEqualTo(to);
		assertThat(response.getBody().getStatus()).isEqualByComparingTo(PENDING);
	}

	@Test
	void testApprove() throws Exception {
		LeaveRequest saved = repository.save(new LeaveRequest("Alice", of(2019, 11, 30), of(2019, 12, 3), PENDING));
		ResponseEntity<LeaveRequestDTO> response = resttemplate.postForEntity("/approve/{id}", null,
				LeaveRequestDTO.class, saved.getId());
		assertThat(response.getStatusCode()).isEqualByComparingTo(ACCEPTED);
		assertThat(response.getHeaders().getContentType()).isEqualByComparingTo(APPLICATION_JSON);
		assertThat(response.getBody().getEmployee()).isEqualTo("Alice");
		assertThat(response.getBody().getStatus()).isEqualByComparingTo(APPROVED);
	}

	@Test
	void testApproveMissing() throws Exception {
		ResponseEntity<LeaveRequestDTO> response = resttemplate.postForEntity("/approve/{id}", null,
				LeaveRequestDTO.class, UUID.randomUUID());
		assertThat(response.getStatusCode()).isEqualByComparingTo(NO_CONTENT);
	}

	@Test
	void testDeny() throws Exception {
		LeaveRequest saved = repository.save(new LeaveRequest("Alice", of(2019, 11, 30), of(2019, 12, 3), PENDING));
		ResponseEntity<LeaveRequestDTO> response = resttemplate.postForEntity("/deny/{id}", null,
				LeaveRequestDTO.class, saved.getId());
		assertThat(response.getStatusCode()).isEqualByComparingTo(ACCEPTED);
		assertThat(response.getHeaders().getContentType()).isEqualByComparingTo(APPLICATION_JSON);
		assertThat(response.getBody().getEmployee()).isEqualTo("Alice");
		assertThat(response.getBody().getStatus()).isEqualByComparingTo(DENIED);
	}

	@Test
	void testViewId() throws Exception {
		LeaveRequest saved = repository.save(new LeaveRequest("Alice", of(2019, 11, 30), of(2019, 12, 3), PENDING));
		ResponseEntity<LeaveRequestDTO> response = resttemplate.getForEntity("/view/id/{id}",
				LeaveRequestDTO.class, saved.getId());
		assertThat(response.getStatusCode()).isEqualByComparingTo(OK);
		assertThat(response.getHeaders().getContentType()).isEqualByComparingTo(APPLICATION_JSON);
		assertThat(response.getBody().getEmployee()).isEqualTo("Alice");
		assertThat(response.getBody().getStatus()).isEqualByComparingTo(PENDING);
	}

	@Test
	void testViewIdMissing() throws Exception {
		ResponseEntity<LeaveRequestDTO> response = resttemplate.getForEntity("/view/id/{id}", LeaveRequestDTO.class,
				UUID.randomUUID());
		assertThat(response.getStatusCode()).isEqualByComparingTo(NO_CONTENT);
	}

	private static final ParameterizedTypeReference<List<LeaveRequestDTO>> typeref = new ParameterizedTypeReference<>() {
	};

	@Test
	void testViewEmployee() throws Exception {
		repository.save(new LeaveRequest("Alice", of(2019, 11, 30), of(2019, 12, 3), PENDING));
		ResponseEntity<List<LeaveRequestDTO>> response = resttemplate.exchange("/view/employee/{employee}", GET, null,
				typeref, "Alice");
		assertThat(response.getStatusCode()).isEqualByComparingTo(OK);
		assertThat(response.getHeaders().getContentType()).isEqualByComparingTo(APPLICATION_JSON);
		assertThat(response.getBody().get(0).getEmployee()).isEqualTo("Alice");
		assertThat(response.getBody().get(0).getStatus()).isEqualByComparingTo(PENDING);
	}

	@Test
	void testViewAll() throws Exception {
		repository.save(new LeaveRequest("Alice", of(2019, 11, 30), of(2019, 12, 3), PENDING));
		ResponseEntity<List<LeaveRequestDTO>> response = resttemplate.exchange("/view/all", GET, null, typeref);
		assertThat(response.getStatusCode()).isEqualByComparingTo(OK);
		assertThat(response.getHeaders().getContentType()).isEqualByComparingTo(APPLICATION_JSON);
		assertThat(response.getBody().get(0).getEmployee()).isEqualTo("Alice");
		assertThat(response.getBody().get(0).getStatus()).isEqualByComparingTo(PENDING);
	}

	@TestConfiguration
	public static class TestContext {
		private static final String TOKEN_VALID_UNTIL_AUGUST_2022 = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJRSS1mdW1sLTRyTnBrdVZxWVg0elpuZlRUZW1hSkxoZ183Z0dULTZiSFlVIn0.eyJqdGkiOiI4MTAzNTI4MC04YTdjLTRjOTktYWMzMi05MDJkMTY0YWVjZWEiLCJleHAiOjE2NTk3MjcxMzgsIm5iZiI6MCwiaWF0IjoxNTczMzI3MTM4LCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwOTAvYXV0aC9yZWFsbXMvc3ByaW5nLWNsb3VkLWdhdGV3YXktcmVhbG0iLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiOWJhNDA5YmYtM2ZhOC00MjdjLWI4YmMtOGFlNjYwNmUyOTAyIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoic3ByaW5nLWNsb3VkLWdhdGV3YXktY2xpZW50Iiwibm9uY2UiOiJnTVdjMTR1WHZMQk9QVlNTRG96NWFUMWNQSUNoVmZzMXFNVHhSWGJPWDBJIiwiYXV0aF90aW1lIjoxNTczMzI3MTM4LCJzZXNzaW9uX3N0YXRlIjoiYWExMmJlZDgtYTlkNC00YTBlLTgyYjAtNTIxMDUwZTAwODA1IiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJvcGVuaWQgcGhvbmUgbWljcm9wcm9maWxlLWp3dCBvZmZsaW5lX2FjY2VzcyBwcm9maWxlIGVtYWlsIGFkZHJlc3MiLCJ1cG4iOiJzcHJpbmctY2xvdWQtZ2F0ZXdheS11c2VyIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJhZGRyZXNzIjp7fSwiZ3JvdXBzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXSwicHJlZmVycmVkX3VzZXJuYW1lIjoic3ByaW5nLWNsb3VkLWdhdGV3YXktdXNlciJ9.HoQ86lJhILWhvZUfefJdxHdTqb4jZFkdWTKHzOAxhNApc9YIgv1zLLlMAXFJHgL1ITY9R1Ke2-SUqMG8d1djPA6D6YRJn4MpWyiS66qpBuRNHg47c9dc-cBLUkXLy9nBZFget87_ZHNhXG_v3MILhH0VbKIWafOTdoJLhJOSy6aQ44iAMjCKXZgMv7EcfvXmwAquaJGGB_fgUa2FXztnt_Fwq4nLgbkU-UYXIqycJVb1gaBbfHmbPpq4pZrkc2Q_9d5FSLGeHIkGmeq17kJLKDzXmvDFu3J5X43nSIaH_WhqSeKkeYAb5HK_xXJnQhRSzku9kYm3dd7z9GBJuSMpRQ";
		@Bean
		public RestTemplateCustomizer bearerTokenCustomizer() {
			return restTemplate -> restTemplate.getInterceptors().add((request, body, execution) -> {
				request.getHeaders().add("Authorization", "Bearer " + TOKEN_VALID_UNTIL_AUGUST_2022);
				return execution.execute(request, body);
			});
		}
	}

}
