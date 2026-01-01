package com.davidrandoll.automation.engine.backend.api.controllers;

import com.davidrandoll.automation.engine.AutomationEngine;
import com.davidrandoll.automation.engine.backend.api.dtos.ExecuteAutomationRequest;
import com.davidrandoll.automation.engine.backend.api.dtos.ExecuteAutomationResponse;
import com.davidrandoll.automation.engine.backend.api.dtos.PlaygroundEvent;
import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.actions.BaseActionList;
import com.davidrandoll.automation.engine.core.conditions.BaseConditionList;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.AutomationResult;
import com.davidrandoll.automation.engine.core.triggers.BaseTriggerList;
import com.davidrandoll.automation.engine.core.variables.BaseVariableList;
import com.davidrandoll.automation.engine.tracing.ExecutionTrace;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlaygroundController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlaygroundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AutomationEngine automationEngine;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void executeAutomation_Yaml_ShouldReturnSuccess() throws Exception {
        String yaml = "alias: test\ntriggers: []\nactions: []";
        ExecuteAutomationRequest request = new ExecuteAutomationRequest();
        request.setAutomation(yaml);
        request.setFormat(ExecuteAutomationRequest.AutomationFormat.YAML);
        request.setInputs(Map.of("key", "value"));

        Automation automation = new Automation("test-automation", null, null, null, null, null, null);

        EventContext context = new EventContext(PlaygroundEvent.from(Map.of("key", "value")));
        AutomationResult result = AutomationResult.executedWithAdditionalFields(
                automation, context, "success", true, false, Map.of(ExecutionTrace.TRACE_KEY, new ExecutionTrace()));

        when(automationEngine.executeAutomationWithYaml(anyString(), any(EventContext.class)))
                .thenReturn(result);

        mockMvc.perform(post("/automation-engine/playground/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executed").value(true))
                .andExpect(jsonPath("$.result").value("success"))
                .andExpect(jsonPath("$.trace").exists());
    }

    @Test
    void executeAutomation_Json_ShouldReturnSuccess() throws Exception {
        String json = "{\"alias\": \"test\"}";
        ExecuteAutomationRequest request = new ExecuteAutomationRequest();
        request.setAutomation(json);
        request.setFormat(ExecuteAutomationRequest.AutomationFormat.JSON);

        Automation automation = new Automation("test", null, null, null, null, null, null);

        EventContext context = new EventContext(PlaygroundEvent.from(Collections.emptyMap()));
        AutomationResult result = AutomationResult.executed(automation, context, "success");

        when(automationEngine.executeAutomationWithJson(anyString(), any(EventContext.class)))
                .thenReturn(result);

        mockMvc.perform(post("/automation-engine/playground/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executed").value(true))
                .andExpect(jsonPath("$.result").value("success"));
    }

    @Test
    void executeAutomation_ShouldHandleException() throws Exception {
        ExecuteAutomationRequest request = new ExecuteAutomationRequest();
        request.setAutomation("invalid");

        when(automationEngine.executeAutomationWithYaml(anyString(), any(EventContext.class)))
                .thenThrow(new RuntimeException("Test error"));

        mockMvc.perform(post("/automation-engine/playground/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executed").value(false))
                .andExpect(jsonPath("$.error").value("Test error"));
    }

    @Test
    void ensureTracingEnabled_ShouldAddOptionsToYaml() throws Exception {
        String yaml = "alias: test";
        ExecuteAutomationRequest request = new ExecuteAutomationRequest();
        request.setAutomation(yaml);
        request.setFormat(ExecuteAutomationRequest.AutomationFormat.YAML);

        Automation automation = new Automation("test", null, null, null, null, null, null);

        EventContext context = new EventContext(PlaygroundEvent.from(Collections.emptyMap()));
        AutomationResult result = AutomationResult.executed(automation, context, null);
        AutomationResult skipped = AutomationResult.skipped(automation, context);

        // We want to verify that the string passed to executeAutomationWithYaml
        // contains tracing: true
        when(automationEngine.executeAutomationWithYaml(anyString(), any(EventContext.class)))
                .thenAnswer(invocation -> {
                    String arg = invocation.getArgument(0);
                    if (arg.contains("tracing: true")) {
                        return result;
                    }
                    return skipped;
                });

        mockMvc.perform(post("/automation-engine/playground/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executed").value(true));
    }

    @Test
    void ensureTracingEnabled_ShouldAddOptionsToJson() throws Exception {
        String json = "{}";
        ExecuteAutomationRequest request = new ExecuteAutomationRequest();
        request.setAutomation(json);
        request.setFormat(ExecuteAutomationRequest.AutomationFormat.JSON);

        Automation automation = new Automation("test", null, null, null, null, null, null);

        EventContext context = new EventContext(PlaygroundEvent.from(Collections.emptyMap()));
        AutomationResult result = AutomationResult.executed(automation, context, null);
        AutomationResult skipped = AutomationResult.skipped(automation, context);

        when(automationEngine.executeAutomationWithJson(anyString(), any(EventContext.class)))
                .thenAnswer(invocation -> {
                    String arg = invocation.getArgument(0);
                    if (arg.contains("\"tracing\": true")) {
                        return result;
                    }
                    return skipped;
                });

        mockMvc.perform(post("/automation-engine/playground/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executed").value(true));
    }

    @Test
    void ensureTracingEnabled_ShouldAddToExistingOptionsInJson() throws Exception {
        String json = "{\"options\": {}}";
        ExecuteAutomationRequest request = new ExecuteAutomationRequest();
        request.setAutomation(json);
        request.setFormat(ExecuteAutomationRequest.AutomationFormat.JSON);

        Automation automation = new Automation("test", null, null, null, null, null, null);

        EventContext context = new EventContext(PlaygroundEvent.from(Collections.emptyMap()));
        AutomationResult result = AutomationResult.executed(automation, context, null);
        AutomationResult skipped = AutomationResult.skipped(automation, context);

        when(automationEngine.executeAutomationWithJson(anyString(), any(EventContext.class)))
                .thenAnswer(invocation -> {
                    String arg = invocation.getArgument(0);
                    if (arg.contains("\"tracing\": true")) {
                        return result;
                    }
                    return skipped;
                });

        mockMvc.perform(post("/automation-engine/playground/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executed").value(true));
    }
}
