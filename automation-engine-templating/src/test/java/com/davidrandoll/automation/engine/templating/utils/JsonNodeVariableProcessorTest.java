package com.davidrandoll.automation.engine.templating.utils;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.ResultContext;
import com.davidrandoll.automation.engine.templating.AETemplatingProperties;
import com.davidrandoll.automation.engine.templating.TemplateProcessor;
import com.davidrandoll.automation.engine.templating.interceptors.AutomationOptionsInterceptor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JsonNodeVariableProcessorTest {

    @Mock
    private TemplateProcessor templateProcessor;

    private ObjectMapper mapper;
    private JsonNodeVariableProcessor processor;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        processor = new JsonNodeVariableProcessor(templateProcessor, mapper, new AETemplatingProperties());
    }

    @Test
    void testProcessIfNotAutomation_Map() throws IOException {
        Map<String, Object> eventData = new HashMap<>();
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("key", "{{ value }}");

        when(templateProcessor.process(eq("{{ value }}"), eq(eventData), anyString())).thenReturn("processed");

        Map<String, Object> result = processor.processIfNotAutomation(eventData, inputMap);

        assertEquals("processed", result.get("key"));
    }

    @Test
    void testProcessIfNotAutomation_JsonNode_Text() throws IOException {
        Map<String, Object> eventData = new HashMap<>();
        JsonNode input = new TextNode("{{ value }}");

        when(templateProcessor.process(eq("{{ value }}"), eq(eventData), anyString())).thenReturn("processed");

        JsonNode result = processor.processIfNotAutomation(eventData, input);

        assertTrue(result.isTextual());
        assertEquals("processed", result.asText());
    }

    @Test
    void testProcessIfNotAutomation_JsonNode_Object() throws IOException {
        Map<String, Object> eventData = new HashMap<>();
        ObjectNode input = mapper.createObjectNode();
        input.put("key", "{{ value }}");

        when(templateProcessor.process(eq("{{ value }}"), eq(eventData), anyString())).thenReturn("processed");

        JsonNode result = processor.processIfNotAutomation(eventData, input);

        assertTrue(result.isObject());
        assertEquals("processed", result.get("key").asText());
    }

    @Test
    void testProcessIfNotAutomation_JsonNode_Array() throws IOException {
        Map<String, Object> eventData = new HashMap<>();
        ObjectNode obj = mapper.createObjectNode();
        obj.put("key", "{{ value }}");
        JsonNode input = mapper.createArrayNode().add(obj);

        when(templateProcessor.process(eq("{{ value }}"), eq(eventData), anyString())).thenReturn("processed");

        JsonNode result = processor.processIfNotAutomation(eventData, input);

        assertTrue(result.isArray());
        assertEquals("processed", result.get(0).get("key").asText());
    }

    @Test
    void testProcessIfNotAutomation_SkipAutomationFields() {
        Map<String, Object> eventData = new HashMap<>();
        ObjectNode input = mapper.createObjectNode();
        input.put("action", "someAction");
        input.put("key", "{{ value }}");

        // Should NOT call templateProcessor because "action" field is present
        JsonNode result = processor.processIfNotAutomation(eventData, input);

        assertEquals(input, result);
    }

    @Test
    void testProcessIfNotAutomation_Null() {
        JsonNode result = processor.processIfNotAutomation(new HashMap<>(), (JsonNode) null);
        assertEquals(null, result);
    }

    @Test
    void testProcessOnlyString() throws IOException {
        Map<String, Object> eventData = new HashMap<>();
        ObjectNode data = mapper.createObjectNode();
        data.put("key", "{{ value }}");
        data.put("number", 123);

        ResultContext resultContext = new ResultContext(null, null, null, data);

        when(templateProcessor.process("{{ value }}", eventData)).thenReturn("processed");

        JsonNode result = processor.processOnlyString(eventData, resultContext);

        assertEquals("processed", result.get("key").asText());
        assertEquals(123, result.get("number").asInt());
    }

    @Test
    void testGetTemplatingType() {
        // Test with explicit spel
        Map<String, Object> optionsSpel = Map.of("templatingType", "spel");
        assertEquals("spel", processor.getTemplatingType(optionsSpel));

        // Test with explicit pebble
        Map<String, Object> optionsPebble = Map.of("templatingType", "pebble");
        assertEquals("pebble", processor.getTemplatingType(optionsPebble));

        // Test with null options
        assertEquals("pebble", processor.getTemplatingType(null));

        // Test with empty options
        assertEquals("pebble", processor.getTemplatingType(new HashMap<>()));

        // Test with options but no templatingType
        assertEquals("pebble", processor.getTemplatingType(Map.of("other", "value")));
    }

    @Test
    void testGetTemplatingType_WithAutomationOptions() {
        EventContext eventContext = mock(EventContext.class);
        Map<String, Object> automationOptions = Map.of("templatingType", "spel");
        when(eventContext.getMetadata(AutomationOptionsInterceptor.AUTOMATION_OPTIONS_KEY))
                .thenReturn(automationOptions);

        // 1. Block level priority
        Map<String, Object> blockOptions = Map.of("templatingType", "pebble");
        assertEquals("pebble", processor.getTemplatingType(eventContext, blockOptions));

        // 2. Automation level priority
        assertEquals("spel", processor.getTemplatingType(eventContext, null));
        assertEquals("spel", processor.getTemplatingType(eventContext, new HashMap<>()));

        // 3. Default priority
        when(eventContext.getMetadata(AutomationOptionsInterceptor.AUTOMATION_OPTIONS_KEY)).thenReturn(null);
        assertEquals("pebble", processor.getTemplatingType(eventContext, null));
    }

    @Test
    void testProcessIfNotAutomation_WithSpelOption() throws IOException {
        Map<String, Object> eventData = new HashMap<>();
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("key", "#{ value }");
        inputMap.put("options", Map.of("templatingType", "spel"));

        when(templateProcessor.process(eq("#{ value }"), eq(eventData), eq("spel"))).thenReturn("processed");

        Map<String, Object> result = processor.processIfNotAutomation(eventData, inputMap);

        assertEquals("processed", result.get("key"));
    }
}
