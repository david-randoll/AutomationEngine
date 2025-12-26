package com.davidrandoll.automation.engine.templating.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.triggers.TriggerContext;
import com.davidrandoll.automation.engine.core.triggers.interceptors.ITriggerChain;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TriggerTemplatingInterceptorTest {

    @Mock
    private JsonNodeVariableProcessor processor;

    private ObjectMapper objectMapper;
    private TriggerTemplatingInterceptor interceptor;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        interceptor = new TriggerTemplatingInterceptor(processor, objectMapper);
    }

    @Test
    void testIntercept() {
        EventContext eventContext = mock(EventContext.class);
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        TriggerContext triggerContext = new TriggerContext(null, null, null, data);

        ITriggerChain chain = mock(ITriggerChain.class);
        when(chain.autoEvaluateExpression()).thenReturn(true);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("some", "data");
        when(eventContext.getEventData(objectMapper)).thenReturn(eventData);

        when(processor.getTemplatingType(any(), any())).thenReturn("pebble");
        when(processor.processIfNotAutomation(eq(eventData), eq(data), eq("pebble"))).thenReturn(data);

        interceptor.intercept(eventContext, triggerContext, chain);

        verify(processor).processIfNotAutomation(eq(eventData), eq(data), eq("pebble"));
        verify(chain).isTriggered(eq(eventContext), any(TriggerContext.class));
    }

    @Test
    void testIntercept_AutoEvaluateFalse() {
        EventContext eventContext = mock(EventContext.class);
        TriggerContext triggerContext = new TriggerContext(null, null, null, new HashMap<>());
        ITriggerChain chain = mock(ITriggerChain.class);
        when(chain.autoEvaluateExpression()).thenReturn(false);

        interceptor.intercept(eventContext, triggerContext, chain);

        verify(chain).isTriggered(eventContext, triggerContext);
    }
}
