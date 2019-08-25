package com.jdriven.service;

import com.jdriven.model.Spreadsheet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SpreadsheetService {

	@PreAuthorize("hasPermission(#spreadsheet, 'READ')")
	public void read(Spreadsheet spreadsheet) {
		log.info("Reading {} as {}", spreadsheet,
				SecurityContextHolder.getContext().getAuthentication().getPrincipal());
	}

	@PreAuthorize("hasPermission(#id, 'com.jdriven.model.Spreadsheet', 'READ')")
	public void readById(Long id) {
		log.info("Reading Spreadsheet id {} as {}", id,
				SecurityContextHolder.getContext().getAuthentication().getPrincipal());

	}

	@PreAuthorize("hasPermission(#spreadsheet, 'WRITE')")
	public void write(Spreadsheet spreadsheet) {
		log.info("Writing {} as {}", spreadsheet,
				SecurityContextHolder.getContext().getAuthentication().getPrincipal());
	}

	@PreAuthorize("hasPermission(#id, 'com.jdriven.model.Spreadsheet', 'WRITE')")
	public void writeById(Long id) {
		log.info("Writing Spreadsheet id {} as {}", id,
				SecurityContextHolder.getContext().getAuthentication().getPrincipal());
	}

}
