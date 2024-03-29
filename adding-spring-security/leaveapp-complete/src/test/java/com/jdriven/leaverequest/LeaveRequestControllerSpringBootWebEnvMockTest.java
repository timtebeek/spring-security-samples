package com.jdriven.leaverequest;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static com.jdriven.leaverequest.LeaveRequest.Status.APPROVED;
import static com.jdriven.leaverequest.LeaveRequest.Status.PENDING;
import static java.time.LocalDate.of;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
class LeaveRequestControllerSpringBootWebEnvMockTest {

	@Autowired
	private LeaveRequestRepository repository;

	@Autowired
	private MockMvc mockmvc;

	@BeforeEach
	void beforeEach() {
		repository.clear();
	}

	@Nested
	class AuthorizeUser {

		@Test
		void testRequest() throws Exception {
			mockmvc.perform(post("/request/{employee}", "Alice")
					.param("from", "2022-11-30")
					.param("to", "2022-12-03")
					.with(jwt().jwt(builder -> builder.subject("Alice"))))
					.andExpectAll(
							status().isAccepted(),
							content().contentType(MediaType.APPLICATION_JSON),
							jsonPath("$.employee").value("Alice"),
							jsonPath("$.status").value("PENDING"));
		}

		@Test
		void testViewRequest() throws Exception {
			LeaveRequest saved = repository
					.save(new LeaveRequest("Alice", of(2022, 11, 30), of(2022, 12, 3), APPROVED));
			mockmvc.perform(get("/view/request/{id}", saved.getId())
					.with(jwt().jwt(builder -> builder.subject("Alice"))))
					.andExpectAll(
							status().isOk(),
							content().contentType(MediaType.APPLICATION_JSON),
							jsonPath("$.employee").value("Alice"),
							jsonPath("$.status").value("APPROVED"));
		}

		@Test
		void testViewEmployee() throws Exception {
			repository.save(new LeaveRequest("Alice", of(2022, 11, 30), of(2022, 12, 3), APPROVED));
			mockmvc.perform(get("/view/employee/{employee}", "Alice")
					.with(jwt().jwt(builder -> builder.subject("Alice"))))
					.andExpectAll(
							status().isOk(),
							content().contentType(MediaType.APPLICATION_JSON),
							jsonPath("$[0].employee").value("Alice"),
							jsonPath("$[0].status").value("APPROVED"));
		}

	}

	@Nested
	class AuthorizeRole {

		@Test
		void testApprove() throws Exception {
			LeaveRequest saved = repository
					.save(new LeaveRequest("Alice", of(2022, 11, 30), of(2022, 12, 3), PENDING));
			mockmvc.perform(post("/approve/{id}", saved.getId())
					.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_HR"))))
					.andExpectAll(
							status().isAccepted(),
							content().contentType(MediaType.APPLICATION_JSON),
							jsonPath("$.employee").value("Alice"),
							jsonPath("$.status").value("APPROVED"));
		}

		@Test
		void testApproveMissing() throws Exception {
			mockmvc.perform(post("/approve/{id}", UUID.randomUUID())
					.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_HR"))))
					.andExpect(status().isNoContent());
		}

		@Test
		void testDeny() throws Exception {
			LeaveRequest saved = repository.save(new LeaveRequest("Alice", of(2022, 11, 30), of(2022, 12, 3), PENDING));
			mockmvc.perform(post("/deny/{id}", saved.getId())
					.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_HR"))))
					.andExpectAll(
							status().isAccepted(),
							content().contentType(MediaType.APPLICATION_JSON),
							jsonPath("$.employee").value("Alice"),
							jsonPath("$.status").value("DENIED"));
		}

		@Test
		void testViewRequestMissing() throws Exception {
			mockmvc.perform(get("/view/request/{id}", UUID.randomUUID())
					// Alice would get a AccessDeniedException on missing returnObject
					.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_HR"))))
					.andExpect(status().isNoContent());
		}

		@Test
		void testViewAll() throws Exception {
			repository.save(new LeaveRequest("Alice", of(2022, 11, 30), of(2022, 12, 3), APPROVED));
			mockmvc.perform(get("/view/all")
					.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_HR"))))
					.andExpectAll(
							status().isOk(),
							content().contentType(MediaType.APPLICATION_JSON),
							jsonPath("$[0].employee").value("Alice"),
							jsonPath("$[0].status").value("APPROVED"));
		}

	}

}
