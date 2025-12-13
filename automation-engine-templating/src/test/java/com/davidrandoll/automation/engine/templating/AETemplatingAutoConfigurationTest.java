package com.davidrandoll.automation.engine.templating;

import com.davidrandoll.automation.engine.templating.interceptors.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = AutomationEngineApplication.class)
class AETemplatingAutoConfigurationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void testTemplateProcessorBeanExists() {
        assertThat(context.containsBean("templateProcessor")).isTrue();
        TemplateProcessor processor = context.getBean(TemplateProcessor.class);
        assertThat(processor).isNotNull();
    }

    @Test
    void testActionTemplatingInterceptorBeanExists() {
        ActionTemplatingInterceptor interceptor = context.getBean(ActionTemplatingInterceptor.class);
        assertThat(interceptor).isNotNull();
    }

    @Test
    void testConditionTemplatingInterceptorBeanExists() {
        ConditionTemplatingInterceptor interceptor = context.getBean(ConditionTemplatingInterceptor.class);
        assertThat(interceptor).isNotNull();
    }

    @Test
    void testVariableTemplatingInterceptorBeanExists() {
        VariableTemplatingInterceptor interceptor = context.getBean(VariableTemplatingInterceptor.class);
        assertThat(interceptor).isNotNull();
    }

    @Test
    void testTriggerTemplatingInterceptorBeanExists() {
        TriggerTemplatingInterceptor interceptor = context.getBean(TriggerTemplatingInterceptor.class);
        assertThat(interceptor).isNotNull();
    }

    @Test
    void testResultTemplatingInterceptorBeanExists() {
        ResultTemplatingInterceptor interceptor = context.getBean(ResultTemplatingInterceptor.class);
        assertThat(interceptor).isNotNull();
    }
}

