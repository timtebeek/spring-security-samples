package com.jdriven.service;

import com.jdriven.model.Spreadsheet;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SpreadsheetService {

	@Secured("com.jdriven.model.Spreadsheet")
	public void read(Spreadsheet spreadsheet) {
		log.info("Reading {}", spreadsheet);
	}

}
