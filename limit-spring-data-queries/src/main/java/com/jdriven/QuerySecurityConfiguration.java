package com.jdriven;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class QuerySecurityConfiguration {
	@Bean
	public SecurityEvaluationContextExtension securityEvaluationContextExtension() {
		return new SecurityEvaluationContextExtension();
	}
}
