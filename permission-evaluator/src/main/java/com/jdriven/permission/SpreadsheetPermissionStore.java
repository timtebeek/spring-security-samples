package com.jdriven.permission;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Repository;

import lombok.Value;

@Repository
@Value
public class SpreadsheetPermissionStore {

	private Set<SpreadsheetPermission> permissions = new HashSet<>();

}
