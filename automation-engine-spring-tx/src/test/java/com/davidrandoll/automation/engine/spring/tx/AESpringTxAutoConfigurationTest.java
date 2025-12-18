package com.davidrandoll.automation.engine.spring.tx;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {AESpringTxAutoConfiguration.class})
@Import(TestConfig.class)
class AESpringTxAutoConfigurationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void testAutoConfigurationLoads() {
        assertThat(context).isNotNull();
    }

    @Test
    void testTransactionalRunnerBeanExists() {
        assertThat(context.containsBean("transactionalRunner")).isTrue();
        assertThat(context.getBean(TransactionalRunner.class)).isNotNull();
    }

    @Test
    void testTransactionalExecutionInterceptorBeanExists() {
        assertThat(context.containsBean("transactionalExecutionInterceptor")).isTrue();
    }
}
