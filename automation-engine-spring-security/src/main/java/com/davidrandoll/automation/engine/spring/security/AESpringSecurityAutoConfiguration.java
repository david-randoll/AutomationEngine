package com.davidrandoll.automation.engine.spring.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(AESpringSecurityConfig.class)
public class AESpringSecurityAutoConfiguration {
}