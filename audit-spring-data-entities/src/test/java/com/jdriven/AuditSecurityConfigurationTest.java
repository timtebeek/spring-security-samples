package com.jdriven;

import static org.assertj.core.api.Assertions.assertThat;

import com.jdriven.repo.Author;
import com.jdriven.repo.Blogpost;
import com.jdriven.repo.BlogpostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureTestEntityManager
class AuditSecurityConfigurationTest {

	@Autowired
	private BlogpostRepository blogpostRepo;
	@Autowired
	private TestEntityManager em;

	@BeforeEach
	void setup() {
		// Ensure active user can be looked up
		Author author = new Author();
		author.setName("test-user");
		em.persist(author);
	}

	@Test
	@Transactional
	@WithMockUser("test-user")
	void testSaveWhileLoggedInAsExistingUser() {
		// Create and save a blogpost
		Blogpost blogpost = new Blogpost();
		blogpost.setTitle("Auditing Spring Data Entities");
		Long id = blogpostRepo.save(blogpost).getId();

		// Verify that author was set by JPA
		Blogpost found = em.find(Blogpost.class, id);
		assertThat(found.getCreatedBy()).hasValueSatisfying(a -> assertThat(a.getName()).isEqualTo("test-user"));
	}

	@Test
	@Transactional
	@WithMockUser("non-existing-user")
	void testSaveWhileLoggedInAsNonExistingUser() {
		// Create and save a blogpost
		Blogpost blogpost = new Blogpost();
		blogpost.setTitle("Auditing Spring Data Entities");
		Long id = blogpostRepo.save(blogpost).getId();

		// Verify that author was not set by JPA
		Blogpost found = em.find(Blogpost.class, id);
		assertThat(found.getCreatedBy()).isEmpty();
	}

	@Test
	@Transactional
	@WithAnonymousUser
	void testSaveAsAnonymousUser() {
		// Create and save a blogpost
		Blogpost blogpost = new Blogpost();
		blogpost.setTitle("Auditing Spring Data Entities");
		Long id = blogpostRepo.save(blogpost).getId();

		// Verify that author was not set by JPA
		Blogpost found = em.find(Blogpost.class, id);
		assertThat(found.getCreatedBy()).isEmpty();
	}

}
