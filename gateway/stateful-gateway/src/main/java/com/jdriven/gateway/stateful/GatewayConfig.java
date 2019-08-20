package com.jdriven.gateway.stateful;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("gateway")
public class GatewayConfig {

	private Map<String, String> routes = new HashMap<>();

	public Map<String, String> getRoutes() {
		return routes;
	}

	public String backend(String service) {
		return routes.get(service);
	}
}
