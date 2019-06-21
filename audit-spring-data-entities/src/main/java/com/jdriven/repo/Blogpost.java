package com.jdriven.repo;

import javax.persistence.Entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.jpa.domain.AbstractAuditable;

@Data
@Entity
@EqualsAndHashCode(of = {}, callSuper = true)
@ToString(callSuper = true)
public class Blogpost extends AbstractAuditable<Author, Long> {

	private String title;
	private String content;
	private boolean published;

}
