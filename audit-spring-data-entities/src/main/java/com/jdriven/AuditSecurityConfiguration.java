package com.jdriven;

import java.util.Optional;

import com.jdriven.repo.Author;
import com.jdriven.repo.AuthorRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableJpaAuditing
public class AuditSecurityConfiguration {
	@Bean
	AuditorAware<Author> auditorAware(AuthorRepository repo) {
		// Lookup Author instance corresponding to logged in user
		return () -> Optional.ofNullable(SecurityContextHolder.getContext())
				.map(SecurityContext::getAuthentication)
				.filter(Authentication::isAuthenticated)
				.map(Authentication::getName)
				.flatMap(repo::findByName);
	}
}
