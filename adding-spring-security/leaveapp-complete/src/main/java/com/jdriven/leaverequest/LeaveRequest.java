package com.jdriven.leaverequest;

import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequest {

	private final UUID id = UUID.randomUUID();

	private String employee;
	private LocalDate fromDate;
	private LocalDate toDate;

	@Builder.Default
	private Status status = Status.PENDING;

	public enum Status {
		PENDING, APPROVED, DENIED
	}

}
