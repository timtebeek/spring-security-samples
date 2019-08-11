package com.jdriven.gateway.stateful;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.route.builder.UriSpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.lang.String.format;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;


@Configuration
public class RoutesConfig {

	@Bean
	public RouterFunction<ServerResponse> indexRouter(@Value("classpath:/public/index.html") final Resource index) {
		return route(GET("/"), request -> ok().contentType(MediaType.TEXT_HTML).syncBody(index));
	}


	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		//TODO: externalize (or leave intentionally plain)
		final Map<String, String> routes = new HashMap<>();
		routes.put("service1", "http://localhost:8081");
		routes.put("service2", "http://localhost:8082");

		final RouteLocatorBuilder.Builder routeBuilder = builder.routes();
		routes.forEach(createRoute(routeBuilder));

		return routeBuilder.build();
	}


	private BiConsumer<String, String> createRoute(final RouteLocatorBuilder.Builder routeBuilder) {
		return (name, url) -> routeBuilder.route(r -> r.path(format("/api/%s/**", name))
			.filters(rewriteAndFilter(name))
			.uri(url));
	}

	// remove "/api/<service_name>/ from path
	private Function<GatewayFilterSpec, UriSpec> rewriteAndFilter(final String serviceName) {
		return gfs -> filter(gfs).rewritePath(format("/api/%s/?(?<remaining>.*)", serviceName), "/${remaining}");
	}

	private GatewayFilterSpec filter(GatewayFilterSpec gfs) {
		return gfs

			// do not pass client-auth stuff to backend services
			.removeRequestHeader("cookie")
			.removeRequestHeader("x-xsrf-token") //TODO: determine to include/exclude CSRF from this example (as it complicates client code)

			// do not allow (overriding of) cookies from services
			.removeResponseHeader("set-cookie");
	}
}
