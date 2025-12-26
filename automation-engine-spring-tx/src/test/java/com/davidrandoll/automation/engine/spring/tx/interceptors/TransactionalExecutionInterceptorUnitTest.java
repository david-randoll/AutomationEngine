package com.davidrandoll.automation.engine.spring.tx.interceptors;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.automation.engine.orchestrator.interceptors.IAutomationExecutionChain;
import com.davidrandoll.automation.engine.spring.tx.TransactionalRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionalExecutionInterceptorUnitTest {

    @Mock
    private TransactionalRunner transactionalRunner;

    @Mock
    private IAutomationExecutionChain chain;

    @Mock
    private EventContext context;

    private TransactionalExecutionInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new TransactionalExecutionInterceptor(transactionalRunner);
    }

    @ParameterizedTest
    @MethodSource("transactionalOptions")
    void testIsTransactional_True(Object value) {
        Automation automation = createAutomationWithOption("transactional", value);
        
        // Mock transactionalRunner to execute the supplier
        when(transactionalRunner.runInTransaction(any())).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        interceptor.intercept(automation, context, chain);

        verify(transactionalRunner).runInTransaction(any());
        verify(chain).proceed(automation, context);
    }

    static Stream<Arguments> transactionalOptions() {
        return Stream.of(
                Arguments.of(true),
                Arguments.of("true"),
                Arguments.of("TRUE"),
                Arguments.of("yes"),
                Arguments.of("YES"),
                Arguments.of("1"),
                Arguments.of(1),
                Arguments.of(100),
                Arguments.of(-1)
        );
    }

    @ParameterizedTest
    @MethodSource("nonTransactionalOptions")
    void testIsTransactional_False(Object value) {
        Automation automation = createAutomationWithOption("transactional", value);

        interceptor.intercept(automation, context, chain);

        verify(transactionalRunner, never()).runInTransaction(any());
        verify(chain).proceed(automation, context);
    }

    static Stream<Arguments> nonTransactionalOptions() {
        return Stream.of(
                Arguments.of(false),
                Arguments.of("false"),
                Arguments.of("no"),
                Arguments.of("0"),
                Arguments.of(0),
                Arguments.of(0.0),
                Arguments.of("random"),
                Arguments.of(Collections.emptyList()),
                Arguments.of(new Object())
        );
    }

    @Test
    void testIsTransactional_NullOption() {
        Automation automation = createAutomationWithOption("other", "value");

        interceptor.intercept(automation, context, chain);

        verify(transactionalRunner, never()).runInTransaction(any());
        verify(chain).proceed(automation, context);
    }

    @Test
    void testIsTransactional_NullOptions() {
        Automation automation = new Automation("test", null, null, null, null, null, null);

        interceptor.intercept(automation, context, chain);

        verify(transactionalRunner, never()).runInTransaction(any());
        verify(chain).proceed(automation, context);
    }

    private Automation createAutomationWithOption(String key, Object value) {
        return new Automation("test", Map.of(key, value), null, null, null, null, null);
    }
}
