package com.jdriven.service;

import com.jdriven.model.Spreadsheet;
import com.jdriven.permission.SpreadsheetPrintAccess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SpreadsheetService {

	@PreAuthorize("hasPermission(#spreadsheet, 'READ')")
	public void read(Spreadsheet spreadsheet) {
		log.info("Reading {}", spreadsheet);
	}

	@PreAuthorize("hasPermission(#id, 'com.jdriven.model.Spreadsheet', 'READ')")
	public void readById(Long id) {
		log.info("Reading Spreadsheet id {} ", id);
	}

	@PreAuthorize("hasPermission(#spreadsheet, 'WRITE')")
	public void write(Spreadsheet spreadsheet) {
		log.info("Writing {} ", spreadsheet);
	}

	@PreAuthorize("hasPermission(#id, 'com.jdriven.model.Spreadsheet', 'WRITE')")
	public void writeById(Long id) {
		log.info("Writing Spreadsheet id {} ", id);
	}

	@SpreadsheetPrintAccess
	public void print(Spreadsheet spreadsheet) {
		log.info("Printing {}", spreadsheet);
	}

}
