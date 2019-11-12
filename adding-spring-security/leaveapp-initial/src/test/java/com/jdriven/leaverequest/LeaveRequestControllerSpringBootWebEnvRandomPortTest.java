package com.jdriven.leaverequest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.jdriven.leaverequest.LeaveRequest;
import com.jdriven.leaverequest.LeaveRequestDTO;
import com.jdriven.leaverequest.LeaveRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
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

}
