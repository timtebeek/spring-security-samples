package com.jdriven.permission;

import java.io.Serializable;

import com.jdriven.model.Spreadsheet;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomPermissionEvaluator implements PermissionEvaluator {

	private final SpreadsheetPermissionStore store;

	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		User principal = (User) authentication.getPrincipal();

		if (targetDomainObject instanceof Spreadsheet spreadsheet) {
			return hasSpreadsheetPermission(principal, spreadsheet, permission);
		}

		return false;
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
			Object permission) {
		User principal = (User) authentication.getPrincipal();

		if (targetType.equals(Spreadsheet.class.getName())) {
			return hasSpreadsheetPermission(principal, targetId, permission);
		}

		return false;
	}

	private boolean hasSpreadsheetPermission(User principal, Spreadsheet spreadsheet, Object permission) {
		boolean hasPermission = store.getPermissions().stream().anyMatch(p -> p.getUser().equals(principal)
				&& p.getSpreadsheet().equals(spreadsheet)
				&& p.getLevel().equals(permission));
		if (!hasPermission) {
			log.warn("Denying {} {} access to {}", principal, permission, spreadsheet);
		}
		return hasPermission;
	}

	private boolean hasSpreadsheetPermission(User principal, Serializable targetId, Object permission) {
		boolean hasPermission = store.getPermissions().stream().anyMatch(p -> p.getUser().equals(principal)
				&& p.getSpreadsheet().getId().equals(targetId)
				&& p.getLevel().equals(permission));
		if (!hasPermission) {
			log.warn("Denying {} {} access to Spreadsheet with id {}", principal, permission, targetId);
		}
		return hasPermission;
	}
}
