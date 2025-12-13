package com.davidrandoll.automation.engine.templating.interceptors;

import com.davidrandoll.automation.engine.core.conditions.ConditionContext;
import com.davidrandoll.automation.engine.core.conditions.interceptors.IConditionChain;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.templating.utils.JsonNodeVariableProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConditionTemplatingInterceptorTest {

    @Mock
    private JsonNodeVariableProcessor processor;

    private ObjectMapper objectMapper;
    private ConditionTemplatingInterceptor interceptor;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        interceptor = new ConditionTemplatingInterceptor(processor, objectMapper);
    }

    @Test
    void testIntercept() {
        EventContext eventContext = mock(EventContext.class);
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        ConditionContext conditionContext = new ConditionContext(data);

        IConditionChain chain = mock(IConditionChain.class);
        when(chain.autoEvaluateExpression()).thenReturn(true);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("some", "data");
        when(eventContext.getEventData(objectMapper)).thenReturn(eventData);

        when(processor.processIfNotAutomation(eq(eventData), eq(data))).thenReturn(data);

        interceptor.intercept(eventContext, conditionContext, chain);

        verify(processor).processIfNotAutomation(eq(eventData), eq(data));
        verify(chain).isSatisfied(eq(eventContext), any(ConditionContext.class));
    }
}
