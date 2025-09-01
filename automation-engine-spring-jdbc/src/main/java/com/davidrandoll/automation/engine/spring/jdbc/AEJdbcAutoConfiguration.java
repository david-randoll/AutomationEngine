package com.davidrandoll.automation.engine.spring.jdbc;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(AEJdbcConfig.class)
public class AEJdbcAutoConfiguration {
}
