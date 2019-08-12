package com.jdriven.gateway.stateful;

import com.jdriven.gateway.stateful.filter.ClientProvidingWebClientFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.WebClientWriteResponseFilter;
import org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;


@Configuration
public class GatewayConfig {

	@Bean
	WebClient tokenAugmentingWebClient(final ReactiveClientRegistrationRepository clientRegistrationRepository,
									   final ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
		return WebClient.builder()
			.filter(new ServerOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrationRepository, authorizedClientRepository))
			.build();
	}

	@Bean
	ClientProvidingWebClientFilter clientProvidingWebClientFilter(WebClient webClient,
																  ObjectProvider<List<HttpHeadersFilter>> headersFiltersProvider,
																  final ReactiveOAuth2AuthorizedClientService authorizedClientService) {
		return new ClientProvidingWebClientFilter(webClient, headersFiltersProvider, authorizedClientService);
	}

	@Bean
	WebClientWriteResponseFilter webClientWriteResponseFilter() {
		return new WebClientWriteResponseFilter();
	}
}
