package com.jdriven.access;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Repository;

import lombok.Value;

@Repository
@Value
public class SpreadsheetAccessStore {

	private Set<SpreadsheetAccess> access = new HashSet<>();

}
