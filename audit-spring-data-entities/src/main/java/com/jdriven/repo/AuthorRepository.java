package com.jdriven.repo;

import java.util.Optional;

import org.springframework.data.repository.Repository;

public interface AuthorRepository extends Repository<Author, String> {

	Optional<Author> findByName(String name);

}
