package com.jdriven.gateway.stateful;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(exclude = ReactiveUserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan
public class GatewayApplication {
	public static void main(String... args) {
		SpringApplication.run(GatewayApplication.class);
	}
}
