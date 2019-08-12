package com.jdriven.gateway.stateful;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.cloud.gateway.discovery.GatewayDiscoveryClientAutoConfiguration;

@SpringBootApplication(exclude = {GatewayDiscoveryClientAutoConfiguration.class, ReactiveUserDetailsServiceAutoConfiguration.class})
public class GatewayApplication {
	public static void main(String... args) {
		SpringApplication.run(GatewayApplication.class);
	}
}
