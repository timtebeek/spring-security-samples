package com.jdriven.gateway;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@SpringBootApplication
@Controller
public class TravelGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(TravelGatewayApplication.class, args);
	}

	@GetMapping(value = "/")
	public String getIndex() {
		return "index";
	}

	@GetMapping(value = "/home")
	public String getHome() {
		return "home";
	}

	@GetMapping("/whoami")
	@ResponseBody
	public Map<String, Object> index(
			@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient,
			@AuthenticationPrincipal OidcUser oidcUser) {
		Map<String, Object> model = new HashMap<>();
		model.put("clientName", authorizedClient.getClientRegistration().getClientName());
		model.put("userName", oidcUser.getName());
		model.put("userAttributes", oidcUser.getAttributes());
		return model;
	}

}
