package com.davidrandoll.automation.engine.templating.interceptors;

import com.davidrandoll.automation.engine.core.actions.ActionContext;
import com.davidrandoll.automation.engine.core.actions.interceptors.IActionChain;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActionTemplatingInterceptorTest {

    @Mock
    private JsonNodeVariableProcessor processor;

    private ObjectMapper objectMapper;
    private ActionTemplatingInterceptor interceptor;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        interceptor = new ActionTemplatingInterceptor(processor, objectMapper);
    }

    @Test
    void testIntercept() {
        EventContext eventContext = mock(EventContext.class);
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        ActionContext actionContext = new ActionContext(null, null, null, data);

        IActionChain chain = mock(IActionChain.class);
        when(chain.autoEvaluateExpression()).thenReturn(true);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("some", "data");
        when(eventContext.getEventData(objectMapper)).thenReturn(eventData);

        when(processor.processIfNotAutomation(eq(eventData), eq(data))).thenReturn(data);

        interceptor.intercept(eventContext, actionContext, chain);

        verify(processor).processIfNotAutomation(eq(eventData), eq(data));
        verify(chain).execute(eq(eventContext), any(ActionContext.class));
    }
}
