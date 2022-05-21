package com.jdriven.gateway.stateful;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

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
