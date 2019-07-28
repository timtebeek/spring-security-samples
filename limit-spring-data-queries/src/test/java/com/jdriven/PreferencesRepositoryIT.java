package com.jdriven;

import java.util.Optional;

import com.jdriven.repo.Preferences;
import com.jdriven.repo.PreferencesRepository;
import com.jdriven.repo.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestEntityManager
@Transactional
class PreferencesRepositoryIT {

	@Autowired
	private PreferencesRepository preferencesRepo;
	@Autowired
	private TestEntityManager em;

	private User userAlice;

	@BeforeEach
	void setup() {
		// Alice does not have preferences yet
		User alice = new User();
		alice.setName("alice");
		this.userAlice = em.persist(alice);

		// Bob has preferences stored
		User bob = new User();
		bob.setName("bob");
		bob = em.persist(bob);
		Preferences preferencesBob = new Preferences();
		preferencesBob.setUser(bob);
		preferencesBob.setDarkMode(true);
		em.persist(preferencesBob);
	}

	@Test
	@WithMockUser("alice")
	void testAliceAllowedToSavePreferences() {
		// Save preferences
		Preferences lightpreferences = new Preferences();
		lightpreferences.setUser(userAlice);
		lightpreferences.setDarkMode(true);
		preferencesRepo.save(lightpreferences);
	}

	@Test
	@WithMockUser("bob")
	void testBobAllowedToFindPreferences() {
		// Retrieve preferences
		Optional<Preferences> found = preferencesRepo.findOne();
		assertThat(found).hasValueSatisfying(p -> {
			assertThat(p.getUser().getName()).isEqualTo("bob");
			assertThat(p.isDarkMode()).isTrue();
		});
	}

	@Test
	@WithMockUser("eve")
	void testEveNotAllowedToSaveAlicesPreferences() {
		// Try to save preferences for Alice
		Preferences lightpreferences = new Preferences();
		lightpreferences.setUser(userAlice);
		lightpreferences.setDarkMode(true);
		Assertions.assertThrows(AccessDeniedException.class, () -> {
			preferencesRepo.save(lightpreferences);
		});
	}

	@Test
	@WithMockUser("eve")
	void testEveNotAllowedToFindPreferences() {
		// Retrieve any preferences
		Optional<Preferences> found = preferencesRepo.findOne();
		// Not results as Eve does not have preferences
		assertThat(found).isNotPresent();
	}
}
