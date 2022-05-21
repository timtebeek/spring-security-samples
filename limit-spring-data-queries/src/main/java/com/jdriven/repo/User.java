package com.jdriven.repo;

import javax.persistence.Entity;

import org.springframework.data.jpa.domain.AbstractPersistable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity(name = "myuser")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class User extends AbstractPersistable<Long> {

	private String name;

}
