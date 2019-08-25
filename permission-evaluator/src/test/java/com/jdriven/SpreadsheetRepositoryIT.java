package com.jdriven;

import java.util.Collections;

import com.jdriven.model.Spreadsheet;
import com.jdriven.permission.Permission;
import com.jdriven.permission.PermissionStore;
import com.jdriven.service.SpreadsheetService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
class SpreadsheetRepositoryIT {

	@Autowired
	private PermissionStore store;

	@Autowired
	private SpreadsheetService service;

	private Spreadsheet spreadsheet;

	@BeforeEach
	void setup() {
		User alice = new User("alice", "", Collections.emptyList());
		User bob = new User("bob", "", Collections.emptyList());
		spreadsheet = new Spreadsheet(123L, "alice's spreadsheet");
		store.getPermissions().add(new Permission(alice, spreadsheet, "READ"));
		store.getPermissions().add(new Permission(alice, spreadsheet, "WRITE"));
		store.getPermissions().add(new Permission(bob, spreadsheet, "READ"));
	}

	@Test
	@WithMockUser("alice")
	void testAliceAllowedToReadSpreadsheet() {
		service.read(spreadsheet);
	}

	@Test
	@WithMockUser("alice")
	void testAliceAllowedToReadSpreadsheetById() {
		service.readById(spreadsheet.getId());
	}

	@Test
	@WithMockUser("alice")
	void testAliceAllowedToWriteSpreadsheet() {
		service.write(spreadsheet);
	}

	@Test
	@WithMockUser("alice")
	void testAliceAllowedToWriteSpreadsheetById() {
		service.writeById(spreadsheet.getId());
	}

	@Test
	@WithMockUser("bob")
	void testBobAllowedToReadSpreadsheet() {
		service.read(spreadsheet);
	}

	@Test
	@WithMockUser("bob")
	void testBobAllowedToReadSpreadsheetById() {
		service.readById(spreadsheet.getId());
	}

	@Test
	@WithMockUser("bob")
	void testBobNotAllowedToWriteSpreadsheet() {
		Assertions.assertThrows(AccessDeniedException.class, () -> service.write(spreadsheet));
	}

	@Test
	@WithMockUser("bob")
	void testBobNotAllowedToWriteSpreadsheetById() {
		Assertions.assertThrows(AccessDeniedException.class, () -> service.writeById(spreadsheet.getId()));
	}

	@Test
	@WithMockUser("eve")
	void testEveNotAllowedToReadSpreadsheet() {
		Assertions.assertThrows(AccessDeniedException.class, () -> service.read(spreadsheet));
	}

	@Test
	@WithMockUser("eve")
	void testEveNotAllowedToReadSpreadsheetById() {
		Assertions.assertThrows(AccessDeniedException.class, () -> service.readById(spreadsheet.getId()));
	}
}
