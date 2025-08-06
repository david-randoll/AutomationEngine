package com.davidrandoll.automation.engine.jdbc;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {
    @Bean
    public NamedParameterJdbcTemplate jdbcTemplate() {
        return mock(NamedParameterJdbcTemplate.class);
    }
}