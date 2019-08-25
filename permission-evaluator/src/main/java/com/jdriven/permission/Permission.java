package com.jdriven.permission;

import com.jdriven.model.Spreadsheet;
import lombok.Value;
import org.springframework.security.core.userdetails.User;

@Value
public class Permission {
	User user;
	Spreadsheet spreadsheet;
	String level;
}