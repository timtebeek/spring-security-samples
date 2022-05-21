package com.jdriven.permission;

import com.jdriven.model.Spreadsheet;

import org.springframework.security.core.userdetails.User;

import lombok.Value;

@Value
public class SpreadsheetPermission {
	User user;
	Spreadsheet spreadsheet;
	String level;
}
