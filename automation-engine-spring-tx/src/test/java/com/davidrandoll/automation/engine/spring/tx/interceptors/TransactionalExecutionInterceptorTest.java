package com.davidrandoll.automation.engine.spring.tx.interceptors;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.orchestrator.interceptors.IAutomationExecutionChain;
import com.davidrandoll.automation.engine.spring.modules.events.time_based.TimeBasedEvent;
import com.davidrandoll.automation.engine.spring.tx.AESpringTxAutoConfiguration;
import com.davidrandoll.automation.engine.spring.tx.TestConfig;
import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import({TestConfig.class, AESpringTxAutoConfiguration.class})
class TransactionalExecutionInterceptorTest extends AutomationEngineTest {

    @Autowired
    private TransactionalExecutionInterceptor transactionalExecutionInterceptor;

    @Autowired
    private DataSource dataSource;

    @Test
    void testInterceptorIsAutowired() {
        assertThat(transactionalExecutionInterceptor).isNotNull();
    }

    @Test
    void testAutomationWithTransactionalOptionExecutesInTransaction() {
        var yaml = """
                alias: transactional-automation
                options:
                  transactional: true
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Executing in transaction"
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(context);

        // Verify the automation was triggered
        assertThat(automation.anyTriggerActivated(context)).isTrue();
    }

    @Test
    void testAutomationWithoutTransactionalOptionExecutesWithoutTransaction() {
        var yaml = """
                alias: non-transactional-automation
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Executing without transaction"
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(context);

        // Verify the automation was triggered
        assertThat(automation.anyTriggerActivated(context)).isTrue();
    }

    @Test
    void testAutomationWithTransactionalFalseExecutesWithoutTransaction() {
        var yaml = """
                alias: explicitly-non-transactional
                options:
                  transactional: false
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Explicitly not transactional"
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        engine.publishEvent(context);

        // Verify the automation was triggered
        assertThat(automation.anyTriggerActivated(context)).isTrue();
    }

    @Test
    void testInterceptWithTransactionalTrue() {
        Map<String, Object> options = new HashMap<>();
        options.put("transactional", true);
        
        var automation = createMockAutomation(options);
        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        AtomicBoolean wasInTransaction = new AtomicBoolean(false);
        
        IAutomationExecutionChain chain = (auto, ctx) -> {
            chainCalled.set(true);
            wasInTransaction.set(TransactionSynchronizationManager.isActualTransactionActive());
            return null;
        };
        
        transactionalExecutionInterceptor.intercept(automation, context, chain);
        
        assertThat(chainCalled.get()).isTrue();
        assertThat(wasInTransaction.get()).isTrue();
    }

    @Test
    void testInterceptWithTransactionalFalse() {
        Map<String, Object> options = new HashMap<>();
        options.put("transactional", false);
        
        var automation = createMockAutomation(options);
        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        
        IAutomationExecutionChain chain = (auto, ctx) -> {
            chainCalled.set(true);
            return null;
        };
        
        transactionalExecutionInterceptor.intercept(automation, context, chain);
        
        assertThat(chainCalled.get()).isTrue();
    }

    @Test
    void testInterceptWithoutTransactionalOption() {
        Map<String, Object> options = new HashMap<>();
        
        var automation = createMockAutomation(options);
        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        
        IAutomationExecutionChain chain = (auto, ctx) -> {
            chainCalled.set(true);
            return null;
        };
        
        transactionalExecutionInterceptor.intercept(automation, context, chain);
        
        assertThat(chainCalled.get()).isTrue();
    }

    @Test
    void testInterceptWithNullOptions() {
        var automation = createMockAutomation(null);
        var context = EventContext.of(new TimeBasedEvent(LocalTime.of(10, 0)));
        
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        
        IAutomationExecutionChain chain = (auto, ctx) -> {
            chainCalled.set(true);
            return null;
        };
        
        transactionalExecutionInterceptor.intercept(automation, context, chain);
        
        assertThat(chainCalled.get()).isTrue();
    }

    private Automation createMockAutomation(Map<String, Object> options) {
        return new Automation("test-automation", options, null, null, null, null, null);
    }
}
