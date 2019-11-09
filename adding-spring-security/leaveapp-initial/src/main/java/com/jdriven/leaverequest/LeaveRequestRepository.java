package com.jdriven.leaverequest;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

@Repository
public class LeaveRequestRepository {

	private Map<UUID, LeaveRequest> leaveRequests = new TreeMap<>();

	public LeaveRequest save(LeaveRequest leaveRequest) {
		leaveRequests.put(leaveRequest.getId(), leaveRequest);
		return leaveRequest;
	}

	public Optional<LeaveRequest> findById(UUID uuid) {
		return Optional.ofNullable(leaveRequests.get(uuid));
	}

	public List<LeaveRequest> findByEmployee(String employee) {
		return leaveRequests.values().stream()
				.filter(lr -> lr.getEmployee().equals(employee))
				.collect(Collectors.toList());
	}

	public List<LeaveRequest> findAll() {
		return new ArrayList<>(leaveRequests.values());
	}

	public void clear() {
		leaveRequests.clear();
	}

}
