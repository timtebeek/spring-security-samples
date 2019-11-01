package com.jdriven.permission;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.access.prepost.PreAuthorize;

@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasPermission(#spreadsheet, 'PRINT')")
public @interface SpreadsheetPrintAccess {
	// Meta-annotation
}
