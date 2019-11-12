package com.jdriven.leaverequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import com.jdriven.leaverequest.LeaveRequest.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

	private final LeaveRequestRepository repo;

	@PreAuthorize("#employee == authentication.name or hasRole('HR')")
	public LeaveRequest request(String employee, LocalDate from, LocalDate to) {
		LeaveRequest leaveRequest = LeaveRequest.builder()
				.employee(employee)
				.fromDate(from)
				.toDate(to)
				.build();
		return repo.save(leaveRequest);
	}

	@RolesAllowed("HR")
	public Optional<LeaveRequest> approve(UUID id) {
		Optional<LeaveRequest> found = repo.findById(id);
		found.ifPresent(lr -> lr.setStatus(Status.APPROVED));
		return found;
	}

	@RolesAllowed("HR")
	public Optional<LeaveRequest> deny(UUID id) {
		Optional<LeaveRequest> found = repo.findById(id);
		found.ifPresent(lr -> lr.setStatus(Status.DENIED));
		return found;
	}

	@PostAuthorize("returnObject.orElse(null)?.employee == authentication.name or hasRole('HR')")
	public Optional<LeaveRequest> retrieve(UUID id) {
		return repo.findById(id);
	}

	@PreAuthorize("#employee == authentication.name or hasRole('HR')")
	public List<LeaveRequest> retrieveFor(String employee) {
		return repo.findByEmployee(employee);
	}

	@RolesAllowed("HR")
	public List<LeaveRequest> retrieveAll() {
		return repo.findAll();
	}

}
