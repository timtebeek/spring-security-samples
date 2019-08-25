package com.jdriven.permission;

import java.util.HashSet;
import java.util.Set;

import lombok.Value;
import org.springframework.stereotype.Repository;

@Repository
@Value
public class SpreadsheetPermissionStore {

	private Set<SpreadsheetPermission> permissions = new HashSet<>();

}
