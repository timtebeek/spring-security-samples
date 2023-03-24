package com.jdriven.repo;

import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Blogpost extends AbstractAuditable<Author, Long> {

	private String title;
	private String content;
	private boolean published;

}
