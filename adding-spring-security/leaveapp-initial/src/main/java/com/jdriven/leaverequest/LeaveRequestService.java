package com.jdriven.leaverequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.jdriven.leaverequest.LeaveRequest.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

	private final LeaveRequestRepository repo;

	public LeaveRequest request(String employee, LocalDate from, LocalDate to) {
		LeaveRequest leaveRequest = LeaveRequest.builder()
				.employee(employee)
				.fromDate(from)
				.toDate(to)
				.build();
		return repo.save(leaveRequest);
	}

	public Optional<LeaveRequest> approve(UUID id) {
		Optional<LeaveRequest> found = repo.findById(id);
		found.ifPresent(lr -> lr.setStatus(Status.APPROVED));
		return found;
	}

	public Optional<LeaveRequest> deny(UUID id) {
		Optional<LeaveRequest> found = repo.findById(id);
		found.ifPresent(lr -> lr.setStatus(Status.DENIED));
		return found;
	}

	public Optional<LeaveRequest> retrieve(UUID id) {
		return repo.findById(id);
	}

	public List<LeaveRequest> retrieveFor(String employee) {
		return repo.findByEmployee(employee);
	}

	public List<LeaveRequest> retrieveAll() {
		return repo.findAll();
	}

}
