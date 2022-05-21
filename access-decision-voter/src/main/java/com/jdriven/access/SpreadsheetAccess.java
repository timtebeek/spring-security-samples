package com.jdriven.access;

import com.jdriven.model.Spreadsheet;

import org.springframework.security.core.userdetails.User;

import lombok.Value;

@Value
public class SpreadsheetAccess {
	User user;
	Spreadsheet spreadsheet;
}
