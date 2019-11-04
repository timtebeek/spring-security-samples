package com.jdriven.flights;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FlightsApplicationTest {

	@MockBean
	private JwtDecoder jwtDecoder;

	@Autowired
	private MockMvc mockmvc;

	@Test
	void testGetIndex() throws Exception {
		mockmvc.perform(get("/").with(jwt(builder -> builder.subject("Subject A"))))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Welcome <a href=\"/whoami\"><span>Subject A</span></a>!")));
	}

}
