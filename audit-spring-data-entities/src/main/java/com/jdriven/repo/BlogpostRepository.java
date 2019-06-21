package com.jdriven.repo;

import java.util.List;

import org.springframework.data.repository.Repository;

public interface BlogpostRepository extends Repository<Blogpost, String> {

	Blogpost save(Blogpost entity);

	List<Blogpost> findByTitleAndPublishedIsTrue(String author);

}
