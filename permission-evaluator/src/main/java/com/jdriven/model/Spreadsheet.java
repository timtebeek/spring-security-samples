package com.jdriven.model;

import lombok.Value;
import org.springframework.security.core.userdetails.User;

@Value
public class Spreadsheet {
	Long id;
	String filename;
	User owner;
}
