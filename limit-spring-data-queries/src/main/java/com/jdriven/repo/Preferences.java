package com.jdriven.repo;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.springframework.data.jpa.domain.AbstractPersistable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Preferences extends AbstractPersistable<Long> {

	boolean darkMode;

	@OneToOne(optional = false)
	private User user;

}
