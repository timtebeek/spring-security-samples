package com.jdriven;

import com.jdriven.permission.CustomPermissionEvaluator;

import org.springframework.aop.Advisor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor;
import org.springframework.security.authorization.method.PreAuthorizeAuthorizationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity(prePostEnabled = false)
public class PermissionEvaluatorConfiguration {

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	Advisor preAuthorizeAuthorizationMethodInterceptor(CustomPermissionEvaluator customPermissionEvaluator) {
		DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
		expressionHandler.setPermissionEvaluator(customPermissionEvaluator);

		PreAuthorizeAuthorizationManager authorizationManager = new PreAuthorizeAuthorizationManager();
		authorizationManager.setExpressionHandler(expressionHandler);

		return AuthorizationManagerBeforeMethodInterceptor.preAuthorize(authorizationManager);
	}

}
