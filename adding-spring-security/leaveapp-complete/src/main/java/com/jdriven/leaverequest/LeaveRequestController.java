package com.jdriven.leaverequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.jdriven.leaverequest.LeaveRequest.Status;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.ResponseEntity.accepted;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class LeaveRequestController {

	private final LeaveRequestService service;

	@PostMapping("/request/{employee}")
	public ResponseEntity<LeaveRequestDTO> request(
			@PathVariable String employee,
			@DateTimeFormat(iso = ISO.DATE) @RequestParam LocalDate from,
			@DateTimeFormat(iso = ISO.DATE) @RequestParam LocalDate to) {
		LeaveRequest leaveRequest = service.request(employee, from, to);
		return accepted().body(new LeaveRequestDTO(leaveRequest));
	}

	@PostMapping("/approve/{id}")
	public ResponseEntity<LeaveRequestDTO> approve(@PathVariable UUID id) {
		Optional<LeaveRequest> approved = service.approve(id);
		if (approved.isEmpty()) {
			return noContent().build();
		}
		return accepted().body(new LeaveRequestDTO(approved.get()));
	}

	@PostMapping("/deny/{id}")
	public ResponseEntity<LeaveRequestDTO> deny(@PathVariable UUID id) {
		Optional<LeaveRequest> denied = service.deny(id);
		if (denied.isEmpty()) {
			return noContent().build();
		}
		return accepted().body(new LeaveRequestDTO(denied.get()));
	}

	@GetMapping("/view/id/{id}")
	public ResponseEntity<LeaveRequestDTO> viewId(@PathVariable UUID id) {
		Optional<LeaveRequest> retrieved = service.retrieve(id);
		if (retrieved.isEmpty()) {
			return noContent().build();
		}
		return ok(new LeaveRequestDTO(retrieved.get()));
	}

	@GetMapping("/view/employee/{employee}")
	public List<LeaveRequestDTO> viewEmployee(@PathVariable String employee) {
		return service.retrieveFor(employee).stream()
				.map(LeaveRequestDTO::new)
				.collect(Collectors.toList());
	}

	@GetMapping("/view/all")
	public List<LeaveRequestDTO> viewAll() {
		return service.retrieveAll().stream()
				.map(LeaveRequestDTO::new)
				.collect(Collectors.toList());
	}

}

@Data
@NoArgsConstructor
class LeaveRequestDTO {

	private String employee;
	private LocalDate fromDate;
	private LocalDate toDate;
	private Status status;

	LeaveRequestDTO(LeaveRequest leaveRequest) {
		this.employee = leaveRequest.getEmployee();
		this.fromDate = leaveRequest.getFromDate();
		this.toDate = leaveRequest.getToDate();
		this.status = leaveRequest.getStatus();
	}
}
