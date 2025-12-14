package com.davidrandoll.automation.engine.templating;

import com.davidrandoll.automation.engine.templating.interceptors.*;
import com.davidrandoll.automation.engine.templating.utils.JsonNodeVariableProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that all templating infrastructure beans are properly configured and available.
 * These beans enable template processing ({{ ... }}) throughout the automation engine.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = AutomationEngineApplication.class)
class AETemplatingAutoConfigurationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void shouldConfigureVariableTemplatingInterceptor() {
        VariableTemplatingInterceptor bean = context.getBean(VariableTemplatingInterceptor.class);
        assertThat(bean).isNotNull()
                .as("VariableTemplatingInterceptor should be configured to process {{ ... }} templates in variable values");
    }

    @Test
    void shouldConfigureActionTemplatingInterceptor() {
        ActionTemplatingInterceptor bean = context.getBean(ActionTemplatingInterceptor.class);
        assertThat(bean).isNotNull()
                .as("ActionTemplatingInterceptor should be configured to process {{ ... }} templates in action properties");
    }

    @Test
    void shouldConfigureConditionTemplatingInterceptor() {
        ConditionTemplatingInterceptor bean = context.getBean(ConditionTemplatingInterceptor.class);
        assertThat(bean).isNotNull()
                .as("ConditionTemplatingInterceptor should be configured to process {{ ... }} templates in condition expressions");
    }

    @Test
    void shouldConfigureTriggerTemplatingInterceptor() {
        TriggerTemplatingInterceptor bean = context.getBean(TriggerTemplatingInterceptor.class);
        assertThat(bean).isNotNull()
                .as("TriggerTemplatingInterceptor should be configured to process {{ ... }} templates in trigger expressions");
    }

    @Test
    void shouldConfigureResultTemplatingInterceptor() {
        ResultTemplatingInterceptor bean = context.getBean(ResultTemplatingInterceptor.class);
        assertThat(bean).isNotNull()
                .as("ResultTemplatingInterceptor should be configured to process {{ ... }} templates in result values");
    }

    @Test
    void shouldConfigureJsonNodeVariableProcessor() {
        JsonNodeVariableProcessor bean = context.getBean(JsonNodeVariableProcessor.class);
        assertThat(bean).isNotNull()
                .as("JsonNodeVariableProcessor should be configured to handle JSON variable processing in templates");
    }
}


