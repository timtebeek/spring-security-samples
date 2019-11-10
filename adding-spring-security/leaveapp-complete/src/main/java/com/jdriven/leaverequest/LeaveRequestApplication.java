package com.jdriven.leaverequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@SpringBootApplication
@EnableGlobalMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
public class LeaveRequestApplication {
	public static void main(String[] args) throws Exception {
		SpringApplication.run(LeaveRequestApplication.class, args);
	}
}
