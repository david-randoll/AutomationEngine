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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    void testRegister() {
        Automation automation = mock(Automation.class);
        automationEngine.register(automation);
        verify(orchestrator).registerAutomation(automation);
    }

    @Test
    void testRegisterWithYaml() {
        String yaml = "alias: test";
        Automation automation = mock(Automation.class);
        when(factory.createAutomation("yaml", yaml)).thenReturn(automation);

        automationEngine.registerWithYaml(yaml);

        verify(factory).createAutomation("yaml", yaml);
        verify(orchestrator).registerAutomation(automation);
    }

    @Test
    void testRegisterWithJson() {
        String json = "{\"alias\": \"test\"}";
        Automation automation = mock(Automation.class);
        when(factory.createAutomation("json", json)).thenReturn(automation);

        automationEngine.registerWithJson(json);

        verify(factory).createAutomation("json", json);
        verify(orchestrator).registerAutomation(automation);
    }

    @Test
    void testRemove() {
        Automation automation = mock(Automation.class);
        automationEngine.remove(automation);
        verify(orchestrator).removeAutomation(automation);
    }

    @Test
    void testRemoveAll() {
        automationEngine.removeAll();
        verify(orchestrator).removeAllAutomations();
    }

    @Test
    void testPublishEventContext() {
        EventContext context = mock(EventContext.class);
        automationEngine.publishEvent(context);
        verify(orchestrator).handleEventContext(context);
    }

    @Test
    void testPublishEvent() {
        IEvent event = mock(IEvent.class);
        automationEngine.publishEvent(event);
        verify(orchestrator).handleEvent(event);
    }

    @Test
    void testExecuteAutomation() {
        Automation automation = mock(Automation.class);
        EventContext context = mock(EventContext.class);
        AutomationResult result = mock(AutomationResult.class);
        when(orchestrator.executeAutomation(automation, context)).thenReturn(result);

        AutomationResult actual = automationEngine.executeAutomation(automation, context);

        assertEquals(result, actual);
        verify(orchestrator).executeAutomation(automation, context);
    }

    @Test
    void testExecuteAutomationWithYaml() {
        String yaml = "alias: test";
        EventContext context = mock(EventContext.class);
        Automation automation = mock(Automation.class);
        AutomationResult result = mock(AutomationResult.class);

        when(factory.createAutomation("yaml", yaml)).thenReturn(automation);
        when(orchestrator.executeAutomation(automation, context)).thenReturn(result);

        AutomationResult actual = automationEngine.executeAutomationWithYaml(yaml, context);

        assertEquals(result, actual);
        verify(factory).createAutomation("yaml", yaml);
        verify(orchestrator).executeAutomation(automation, context);
    }

    @Test
    void testExecuteAutomationWithYamlAndEvent() {
        String yaml = "alias: test";
        IEvent event = mock(IEvent.class);
        Automation automation = mock(Automation.class);
        AutomationResult result = mock(AutomationResult.class);

        when(factory.createAutomation("yaml", yaml)).thenReturn(automation);
        when(orchestrator.executeAutomation(eq(automation), any(EventContext.class))).thenReturn(result);

        AutomationResult actual = automationEngine.executeAutomationWithYaml(yaml, event);

        assertEquals(result, actual);
        verify(factory).createAutomation("yaml", yaml);
    }

    @Test
    void testExecuteAutomationWithJson() {
        String json = "{\"alias\": \"test\"}";
        EventContext context = mock(EventContext.class);
        Automation automation = mock(Automation.class);
        AutomationResult result = mock(AutomationResult.class);

        when(factory.createAutomation("json", json)).thenReturn(automation);
        when(orchestrator.executeAutomation(automation, context)).thenReturn(result);

        AutomationResult actual = automationEngine.executeAutomationWithJson(json, context);

        assertEquals(result, actual);
        verify(factory).createAutomation("json", json);
        verify(orchestrator).executeAutomation(automation, context);
    }

    @Test
    void testExecuteAutomationWithJsonAndEvent() {
        String json = "{\"alias\": \"test\"}";
        IEvent event = mock(IEvent.class);
        Automation automation = mock(Automation.class);
        AutomationResult result = mock(AutomationResult.class);

        when(factory.createAutomation("json", json)).thenReturn(automation);
        when(orchestrator.executeAutomation(eq(automation), any(EventContext.class))).thenReturn(result);

        AutomationResult actual = automationEngine.executeAutomationWithJson(json, event);

        assertEquals(result, actual);
        verify(factory).createAutomation("json", json);
    }

    @Test
    void testGetEventFactory() {
        assertEquals(eventFactory, automationEngine.getEventFactory());
    }
}
