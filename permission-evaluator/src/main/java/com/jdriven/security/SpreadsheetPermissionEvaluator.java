package com.jdriven.security;

import java.io.Serializable;

import com.jdriven.model.Spreadsheet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpreadsheetPermissionEvaluator implements PermissionEvaluator {

	private final PermissionStore store;

	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object accessLevel) {
		Spreadsheet spreadsheet = (Spreadsheet) targetDomainObject;
		User principal = (User) authentication.getPrincipal();
		boolean hasPermission = store.getPermissions().stream().anyMatch(p -> p.getUser().equals(principal)
				&& p.getSpreadsheet().equals(spreadsheet)
				&& p.getLevel().equals(accessLevel));
		if (!hasPermission) {
			log.warn("Denying {} {} access to {}", authentication, accessLevel, spreadsheet);
		}
		return hasPermission;
	}

	@Override
	public boolean hasPermission(
			Authentication authentication,
			Serializable targetId,
			String targetType,
			Object accessLevel) {
		if (!targetType.equals(Spreadsheet.class.getName())) {
			log.info("Incorrect targetType {}", targetType);
			return false;
		}

		User principal = (User) authentication.getPrincipal();
		boolean hasPermission = store.getPermissions().stream().anyMatch(p -> p.getUser().equals(principal)
				&& p.getSpreadsheet().getId().equals(targetId)
				&& p.getLevel().equals(accessLevel));
		if (!hasPermission) {
			log.warn("Denying {} {} access to {} with id {}", principal, accessLevel, targetType, targetId);
		}
		return hasPermission;
	}
}
