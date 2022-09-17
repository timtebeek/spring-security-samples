package com.jdriven.leaverequest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.jdriven.leaverequest.LeaveRequest.Status;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import static org.springframework.http.ResponseEntity.accepted;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
class LeaveRequestController {

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
		return service.approve(id)
				.map(lr -> accepted().body(new LeaveRequestDTO(lr)))
				.orElse(noContent().build());
	}

	@PostMapping("/deny/{id}")
	public ResponseEntity<LeaveRequestDTO> deny(@PathVariable UUID id) {
		return service.deny(id)
				.map(lr -> accepted().body(new LeaveRequestDTO(lr)))
				.orElse(noContent().build());
	}

	@GetMapping("/view/request/{id}")
	public ResponseEntity<LeaveRequestDTO> viewRequest(@PathVariable UUID id) {
		return service.retrieve(id)
				.map(lr -> ok().body(new LeaveRequestDTO(lr)))
				.orElse(noContent().build());
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

	@GetMapping("/whoami")
	public JwtAuthenticationToken whoami(JwtAuthenticationToken auth) {
		return auth;
	}

}

@Data
@NoArgsConstructor
class LeaveRequestDTO {

	private UUID id;
	private String employee;
	private LocalDate fromDate;
	private LocalDate toDate;
	private Status status;

	LeaveRequestDTO(LeaveRequest leaveRequest) {
		this.id = leaveRequest.getId();
		this.employee = leaveRequest.getEmployee();
		this.fromDate = leaveRequest.getFromDate();
		this.toDate = leaveRequest.getToDate();
		this.status = leaveRequest.getStatus();
	}
}
