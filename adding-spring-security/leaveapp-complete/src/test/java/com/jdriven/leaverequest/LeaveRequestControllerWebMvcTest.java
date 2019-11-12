package com.jdriven.leaverequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.jdriven.leaverequest.LeaveRequest.Status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static java.time.LocalDate.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LeaveRequestController.class)
@WithMockUser
class LeaveRequestControllerWebMvcTest {

	@MockBean
	private JwtDecoder jwtDecoder;

	@MockBean
	private LeaveRequestService service;

	@Autowired
	private MockMvc mockmvc;

	@Test
	void testRequest() throws Exception {
		when(service.request(anyString(), any(), any()))
				.thenReturn(new LeaveRequest("Alice", of(2019, 11, 30), of(2019, 12, 3), Status.PENDING));
		mockmvc.perform(post("/request/{employee}", "Alice")
				.with(csrf())
				.param("from", "2019-11-30")
				.param("to", "2019-12-03"))
				.andExpect(status().isAccepted())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.employee").value("Alice"))
				.andExpect(jsonPath("$.status").value("PENDING"));
	}

	@Test
	void testApprove() throws Exception {
		when(service.approve(any()))
				.thenReturn(Optional.of(new LeaveRequest("Alice", of(2019, 11, 30), of(2019, 12, 3), Status.APPROVED)));
		mockmvc.perform(post("/approve/{id}", UUID.randomUUID())
				.with(csrf()))
				.andExpect(status().isAccepted())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.employee").value("Alice"))
				.andExpect(jsonPath("$.status").value("APPROVED"));
	}

	@Test
	void testApproveMissing() throws Exception {
		mockmvc.perform(post("/approve/{id}", UUID.randomUUID())
				.with(csrf()))
				.andExpect(status().isNoContent());
	}

	@Test
	void testDeny() throws Exception {
		when(service.deny(any()))
				.thenReturn(Optional.of(new LeaveRequest("Alice", of(2019, 11, 30), of(2019, 12, 3), Status.DENIED)));
		mockmvc.perform(post("/deny/{id}", UUID.randomUUID())
				.with(csrf()))
				.andExpect(status().isAccepted())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.employee").value("Alice"))
				.andExpect(jsonPath("$.status").value("DENIED"));
	}

	@Test
	void testViewId() throws Exception {
		when(service.retrieve(any()))
				.thenReturn(Optional.of(new LeaveRequest("Alice", of(2019, 11, 30), of(2019, 12, 3), Status.APPROVED)));
		mockmvc.perform(get("/view/id/{id}", UUID.randomUUID()))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.employee").value("Alice"))
				.andExpect(jsonPath("$.status").value("APPROVED"));
	}

	@Test
	void testViewIdMissing() throws Exception {
		mockmvc.perform(get("/view/id/{id}", UUID.randomUUID()))
				.andExpect(status().isNoContent());
	}

	@Test
	void testViewEmployee() throws Exception {
		when(service.retrieveFor("Alice"))
				.thenReturn(List.of(new LeaveRequest("Alice", of(2019, 11, 30), of(2019, 12, 3), Status.APPROVED)));
		mockmvc.perform(get("/view/employee/{employee}", "Alice"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$[0].employee").value("Alice"))
				.andExpect(jsonPath("$[0].status").value("APPROVED"));
	}

	@Test
	void testViewAll() throws Exception {
		when(service.retrieveAll())
				.thenReturn(List.of(new LeaveRequest("Alice", of(2019, 11, 30), of(2019, 12, 3), Status.APPROVED)));
		mockmvc.perform(get("/view/all"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$[0].employee").value("Alice"))
				.andExpect(jsonPath("$[0].status").value("APPROVED"));
	}

}
