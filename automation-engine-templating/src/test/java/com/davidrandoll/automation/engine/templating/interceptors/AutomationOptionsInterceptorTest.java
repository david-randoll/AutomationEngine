package com.davidrandoll.automation.engine.templating.interceptors;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.orchestrator.interceptors.IAutomationExecutionChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutomationOptionsInterceptorTest {

    @InjectMocks
    private AutomationOptionsInterceptor interceptor;

    @Mock
    private Automation automation;

    @Mock
    private EventContext eventContext;

    @Mock
    private IAutomationExecutionChain chain;

    @Test
    void testIntercept_AddsOptionsToMetadata() {
        Map<String, Object> options = Map.of("templatingType", "spel");
        when(automation.getOptions()).thenReturn(options);

        interceptor.intercept(automation, eventContext, chain);

        verify(eventContext).addMetadata(AutomationOptionsInterceptor.AUTOMATION_OPTIONS_KEY, options);
        verify(chain).proceed(automation, eventContext);
    }
}
