package com.jdriven.repo;

import org.springframework.data.repository.Repository;

public interface BlogpostRepository extends Repository<Blogpost, String> {

	Blogpost save(Blogpost entity);

}
