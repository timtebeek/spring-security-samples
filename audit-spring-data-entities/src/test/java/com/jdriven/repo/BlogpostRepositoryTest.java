package com.jdriven.repo;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestEntityManager
class BlogpostRepositoryTest {

	@Autowired
	private BlogpostRepository blogpostRepo;
	@Autowired
	private TestEntityManager em;

	@BeforeEach
	void setup() {
		Author author = new Author();
		author.setName("test-user");
		em.persist(author);
	}

	@Test
	@Transactional
	@WithMockUser("test-user")
	void testSave() {
		Blogpost blogpost = new Blogpost();
		blogpost.setTitle("Auditing Spring Data Entities");
		Long id = blogpostRepo.save(blogpost).getId();

		Blogpost found = em.find(Blogpost.class, id);
		assertThat(found.getCreatedBy()).hasValueSatisfying(a -> assertThat(a.getName()).isEqualTo("test-user"));
	}

	@TestConfiguration
	static class Config {

		// bean to map current user to an author by name
		@Bean
		AuditorAware<Author> auditorAware(AuthorRepository repo) {
			return () -> Optional.ofNullable(SecurityContextHolder.getContext())
					.map(SecurityContext::getAuthentication)
					.filter(Authentication::isAuthenticated)
					.map(Authentication::getName)
					.flatMap(repo::findByName);
		}

	}

}
