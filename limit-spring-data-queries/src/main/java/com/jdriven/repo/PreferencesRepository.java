package com.jdriven.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

public interface PreferencesRepository extends Repository<Preferences, Long> {

	@PreAuthorize("#entity.user.name == authentication.name")
	Preferences save(Preferences entity);

	@PostAuthorize("returnObject != null and returnObject.user.name == authentication.name")
	Preferences findById(Long id);

	@Query("select p from #{#entityName} p where p.user.name = ?#{ authentication.name }")
	Optional<Preferences> findOne();

}
