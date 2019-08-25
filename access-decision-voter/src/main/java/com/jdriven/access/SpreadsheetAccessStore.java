package com.jdriven.access;

import java.util.HashSet;
import java.util.Set;

import lombok.Value;
import org.springframework.stereotype.Repository;

@Repository
@Value
public class SpreadsheetAccessStore {

	private Set<SpreadsheetAccess> access = new HashSet<>();

}
