package com.jdriven.leaverequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.jdriven.leaverequest.LeaveRequest.Status.APPROVED;
import static com.jdriven.leaverequest.LeaveRequest.Status.DENIED;
import static com.jdriven.leaverequest.LeaveRequest.Status.PENDING;
import static java.time.LocalDate.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LeaveRequestController.class)
class LeaveRequestControllerWebMvcTest {

	@MockBean
	private LeaveRequestService service;

	@Autowired
	private MockMvc mockmvc;

	@Nested
	class AuthorizeUser {

		@Test
		void testRequest() throws Exception {
			when(service.request(anyString(), any(), any()))
					.thenReturn(new LeaveRequest("Alice", of(2022, 11, 30), of(2022, 12, 3), PENDING));
			mockmvc.perform(post("/request/{employee}", "Alice")
					.param("from", "2022-11-30")
					.param("to", "2022-12-03")
					.with(jwt()))
					.andExpectAll(
							status().isAccepted(),
							content().contentType(MediaType.APPLICATION_JSON),
							jsonPath("$.employee").value("Alice"),
							jsonPath("$.status").value("PENDING"));
		}

		@Test
		void testViewRequest() throws Exception {
			when(service.retrieve(any()))
					.thenReturn(Optional.of(new LeaveRequest("Alice", of(2022, 11, 30), of(2022, 12, 3), APPROVED)));
			mockmvc.perform(get("/view/request/{id}", UUID.randomUUID())
					.with(jwt()))
					.andExpectAll(
							status().isOk(),
							content().contentType(MediaType.APPLICATION_JSON),
							jsonPath("$.employee").value("Alice"),
							jsonPath("$.status").value("APPROVED"));
		}

		@Test
		void testViewEmployee() throws Exception {
			when(service.retrieveFor("Alice"))
					.thenReturn(List.of(new LeaveRequest("Alice", of(2022, 11, 30), of(2022, 12, 3), APPROVED)));
			mockmvc.perform(get("/view/employee/{employee}", "Alice")
					.with(jwt()))
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
			when(service.approve(any()))
					.thenReturn(Optional.of(new LeaveRequest("Alice", of(2022, 11, 30), of(2022, 12, 3), APPROVED)));
			mockmvc.perform(post("/approve/{id}", UUID.randomUUID())
					.with(jwt()))
					.andExpectAll(
							status().isAccepted(),
							content().contentType(MediaType.APPLICATION_JSON),
							jsonPath("$.employee").value("Alice"),
							jsonPath("$.status").value("APPROVED"));
		}

		@Test
		void testApproveMissing() throws Exception {
			mockmvc.perform(post("/approve/{id}", UUID.randomUUID())
					.with(jwt()))
					.andExpect(status().isNoContent());
		}

		@Test
		void testDeny() throws Exception {
			when(service.deny(any()))
					.thenReturn(Optional.of(new LeaveRequest("Alice", of(2022, 11, 30), of(2022, 12, 3), DENIED)));
			mockmvc.perform(post("/deny/{id}", UUID.randomUUID())
					.with(jwt()))
					.andExpectAll(
							status().isAccepted(),
							content().contentType(MediaType.APPLICATION_JSON),
							jsonPath("$.employee").value("Alice"),
							jsonPath("$.status").value("DENIED"));
		}

		@Test
		void testViewRequestMissing() throws Exception {
			mockmvc.perform(get("/view/request/{id}", UUID.randomUUID())
					.with(jwt()))
					.andExpect(status().isNoContent());
		}

		@Test
		void testViewAll() throws Exception {
			when(service.retrieveAll())
					.thenReturn(List.of(new LeaveRequest("Alice", of(2022, 11, 30), of(2022, 12, 3), APPROVED)));
			mockmvc.perform(get("/view/all")
					.with(jwt()))
					.andExpectAll(
							status().isOk(),
							content().contentType(MediaType.APPLICATION_JSON),
							jsonPath("$[0].employee").value("Alice"),
							jsonPath("$[0].status").value("APPROVED"));
		}

	}

}
