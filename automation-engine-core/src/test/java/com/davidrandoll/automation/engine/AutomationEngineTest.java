package com.davidrandoll.automation.engine;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.automation.engine.core.result.AutomationResult;
import com.davidrandoll.automation.engine.creator.AutomationFactory;
import com.davidrandoll.automation.engine.creator.events.EventFactory;
import com.davidrandoll.automation.engine.orchestrator.IAEOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutomationEngineTest {

    @Mock
    private IAEOrchestrator orchestrator;
    @Mock
    private AutomationFactory factory;
    @Mock
    private EventFactory eventFactory;

    private AutomationEngine automationEngine;

    @BeforeEach
    void setUp() {
        automationEngine = new AutomationEngine(orchestrator, factory, eventFactory);
    }

    @Test
    void testRegister_ShouldDelegateToOrchestrator() {
        Automation automation = mock(Automation.class);
        
        automationEngine.register(automation);
        
        verify(orchestrator).registerAutomation(automation);
        verifyNoMoreInteractions(orchestrator);
    }

    @Test
    void testRegisterWithYaml_ShouldParseAndRegister() {
        String yaml = "alias: test-automation";
        Automation automation = mock(Automation.class);
        when(factory.createAutomation("yaml", yaml)).thenReturn(automation);

        automationEngine.registerWithYaml(yaml);

        verify(factory).createAutomation("yaml", yaml);
        verify(orchestrator).registerAutomation(automation);
    }

    @Test
    void testRegisterWithJson_ShouldParseAndRegister() {
        String json = "{\"alias\": \"test-automation\"}";
        Automation automation = mock(Automation.class);
        when(factory.createAutomation("json", json)).thenReturn(automation);

        automationEngine.registerWithJson(json);

        verify(factory).createAutomation("json", json);
        verify(orchestrator).registerAutomation(automation);
    }

    @Test
    void testRemove_ShouldDelegateToOrchestrator() {
        Automation automation = mock(Automation.class);
        
        automationEngine.remove(automation);
        
        verify(orchestrator).removeAutomation(automation);
        verifyNoMoreInteractions(orchestrator);
    }

    @Test
    void testRemoveAll_ShouldClearAllAutomations() {
        automationEngine.removeAll();
        
        verify(orchestrator).removeAllAutomations();
        verifyNoMoreInteractions(orchestrator);
    }

    @Test
    void testPublishEventContext_ShouldHandleEventContext() {
        EventContext context = mock(EventContext.class);
        
        automationEngine.publishEvent(context);
        
        verify(orchestrator).handleEventContext(context);
        verifyNoMoreInteractions(orchestrator);
    }

    @Test
    void testPublishEvent_ShouldHandleEvent() {
        IEvent event = mock(IEvent.class);
        
        automationEngine.publishEvent(event);
        
        verify(orchestrator).handleEvent(event);
        verifyNoMoreInteractions(orchestrator);
    }

    @Test
    void testExecuteAutomation_ShouldReturnResult() {
        Automation automation = mock(Automation.class);
        when(automation.getAlias()).thenReturn("test-automation");
        EventContext context = mock(EventContext.class);
        AutomationResult result = mock(AutomationResult.class);
        when(result.isExecuted()).thenReturn(true);
        when(result.getAutomation()).thenReturn(automation);
        when(orchestrator.executeAutomation(automation, context)).thenReturn(result);

        AutomationResult actual = automationEngine.executeAutomation(automation, context);

        assertEquals(result, actual);
        assertThat(actual.isExecuted()).isTrue();
        assertThat(actual.getAutomation().getAlias()).isEqualTo("test-automation");
        verify(orchestrator).executeAutomation(automation, context);
    }

    @Test
    void testExecuteAutomationWithYaml_ShouldParseAndExecute() {
        String yaml = "alias: execute-test";
        EventContext context = mock(EventContext.class);
        Automation automation = mock(Automation.class);
        AutomationResult result = mock(AutomationResult.class);
        when(result.isExecuted()).thenReturn(true);

        when(factory.createAutomation("yaml", yaml)).thenReturn(automation);
        when(orchestrator.executeAutomation(automation, context)).thenReturn(result);

        AutomationResult actual = automationEngine.executeAutomationWithYaml(yaml, context);

        assertEquals(result, actual);
        assertThat(actual.isExecuted()).isTrue();
        verify(factory).createAutomation("yaml", yaml);
        verify(orchestrator).executeAutomation(automation, context);
    }

    @Test
    void testExecuteAutomationWithYamlAndEvent_ShouldWrapEventInContext() {
        String yaml = "alias: event-test";
        IEvent event = mock(IEvent.class);
        Automation automation = mock(Automation.class);
        when(automation.getAlias()).thenReturn("event-test");
        AutomationResult result = mock(AutomationResult.class);
        when(result.getAutomation()).thenReturn(automation);

        when(factory.createAutomation("yaml", yaml)).thenReturn(automation);
        when(orchestrator.executeAutomation(eq(automation), any(EventContext.class))).thenReturn(result);

        AutomationResult actual = automationEngine.executeAutomationWithYaml(yaml, event);

        assertEquals(result, actual);
        assertThat(actual.getAutomation().getAlias()).isEqualTo("event-test");
        verify(factory).createAutomation("yaml", yaml);
        verify(orchestrator).executeAutomation(eq(automation), any(EventContext.class));
    }

    @Test
    void testExecuteAutomationWithJson_ShouldParseAndExecute() {
        String json = "{\"alias\": \"json-test\"}";
        EventContext context = mock(EventContext.class);
        Automation automation = mock(Automation.class);
        AutomationResult result = mock(AutomationResult.class);
        when(result.isExecuted()).thenReturn(false);

        when(factory.createAutomation("json", json)).thenReturn(automation);
        when(orchestrator.executeAutomation(automation, context)).thenReturn(result);

        AutomationResult actual = automationEngine.executeAutomationWithJson(json, context);

        assertEquals(result, actual);
        assertThat(actual.isExecuted()).isFalse();
        verify(factory).createAutomation("json", json);
        verify(orchestrator).executeAutomation(automation, context);
    }

    @Test
    void testExecuteAutomationWithJsonAndEvent_ShouldWrapEventInContext() {
        String json = "{\"alias\": \"json-event-test\"}";
        IEvent event = mock(IEvent.class);
        Automation automation = mock(Automation.class);
        AutomationResult result = mock(AutomationResult.class);

        when(factory.createAutomation("json", json)).thenReturn(automation);
        when(orchestrator.executeAutomation(eq(automation), any(EventContext.class))).thenReturn(result);

        AutomationResult actual = automationEngine.executeAutomationWithJson(json, event);

        assertEquals(result, actual);
        verify(factory).createAutomation("json", json);
        verify(orchestrator).executeAutomation(eq(automation), any(EventContext.class));
    }

    @Test
    void testGetEventFactory_ShouldReturnInjectedFactory() {
        EventFactory result = automationEngine.getEventFactory();
        
        assertEquals(eventFactory, result);
        assertThat(result).isNotNull();
    }
}
