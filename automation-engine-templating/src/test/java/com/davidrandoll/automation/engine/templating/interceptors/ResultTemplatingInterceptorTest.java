package com.davidrandoll.automation.engine.templating.interceptors;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.ResultContext;
import com.davidrandoll.automation.engine.core.result.interceptors.IResultChain;
import com.davidrandoll.automation.engine.templating.utils.JsonNodeVariableProcessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
class ResultTemplatingInterceptorTest {

    @Mock
    private JsonNodeVariableProcessor processor;

    private ObjectMapper objectMapper;
    private ResultTemplatingInterceptor interceptor;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        interceptor = new ResultTemplatingInterceptor(processor, objectMapper);
    }

    @Test
    void testIntercept() {
        EventContext eventContext = mock(EventContext.class);
        ObjectNode data = objectMapper.createObjectNode();
        data.put("key", "value");
        ResultContext resultContext = new ResultContext(null, null, null, data);

        IResultChain chain = mock(IResultChain.class);
        when(chain.autoEvaluateExpression()).thenReturn(true);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("some", "data");
        when(eventContext.getEventData(objectMapper)).thenReturn(eventData);

        when(processor.getTemplatingType(any())).thenReturn("pebble");
        when(processor.processIfNotAutomation(eq(eventData), any(JsonNode.class), eq("pebble"))).thenReturn(data);

        interceptor.intercept(eventContext, resultContext, chain);

        verify(processor).processIfNotAutomation(eq(eventData), any(JsonNode.class), eq("pebble"));
        verify(chain).getExecutionSummary(eq(eventContext), any(ResultContext.class));
    }

    @Test
    void testIntercept_AutoEvaluateFalse() {
        EventContext eventContext = mock(EventContext.class);
        ResultContext resultContext = new ResultContext(null, null, null, objectMapper.createObjectNode());
        IResultChain chain = mock(IResultChain.class);
        when(chain.autoEvaluateExpression()).thenReturn(false);

        interceptor.intercept(eventContext, resultContext, chain);

        verify(chain).getExecutionSummary(eventContext, resultContext);
    }
}
