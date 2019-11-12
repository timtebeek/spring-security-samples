package com.jdriven.leaverequest;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;

import static com.jdriven.leaverequest.LeaveRequest.Status.APPROVED;
import static com.jdriven.leaverequest.LeaveRequest.Status.DENIED;
import static com.jdriven.leaverequest.LeaveRequest.Status.PENDING;
import static java.time.LocalDate.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class LeaveRequestServiceTest {

	@MockBean
	private JwtDecoder jwtDecoder;

	@SpyBean
	private LeaveRequestRepository repository;

	@Autowired
	private LeaveRequestService service;

	@Test
	@WithMockUser("Alice")
	void testRequest() {
		LeaveRequest leaveRequest = service.request("Alice", of(2019, 11, 30), of(2019, 12, 03));
		verify(repository).save(leaveRequest);
	}

	@Test
	@WithMockUser(roles = "HR")
	void testApprove() {
		LeaveRequest saved = repository.save(new LeaveRequest("Alice", of(2019, 11, 30), of(2019, 12, 03), PENDING));
		Optional<LeaveRequest> approved = service.approve(saved.getId());
		verify(repository).findById(saved.getId());
		assertThat(approved).isPresent();
		assertThat(approved).get().isSameAs(saved);
		assertThat(approved.get().getStatus()).isSameAs(APPROVED);
	}

	@Test
	@WithMockUser(roles = "HR")
	void testDeny() {
		LeaveRequest saved = repository.save(new LeaveRequest("Alice", of(2019, 11, 30), of(2019, 12, 03), PENDING));
		Optional<LeaveRequest> denied = service.deny(saved.getId());
		verify(repository).findById(saved.getId());
		assertThat(denied).isPresent();
		assertThat(denied).get().isSameAs(saved);
		assertThat(denied.get().getStatus()).isSameAs(DENIED);
	}

}
