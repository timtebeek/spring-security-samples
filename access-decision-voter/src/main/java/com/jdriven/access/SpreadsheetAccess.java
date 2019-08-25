package com.jdriven.access;

import com.jdriven.model.Spreadsheet;
import lombok.Value;
import org.springframework.security.core.userdetails.User;

@Value
public class SpreadsheetAccess {
	User user;
	Spreadsheet spreadsheet;
}