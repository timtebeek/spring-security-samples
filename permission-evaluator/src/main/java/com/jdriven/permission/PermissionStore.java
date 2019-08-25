package com.jdriven.permission;

import java.util.HashSet;
import java.util.Set;

import lombok.Value;
import org.springframework.stereotype.Repository;

@Repository
@Value
public class PermissionStore {

	private Set<Permission> permissions = new HashSet<>();

}
