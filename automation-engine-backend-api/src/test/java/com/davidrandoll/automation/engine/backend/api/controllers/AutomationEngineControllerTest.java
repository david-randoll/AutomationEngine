package com.davidrandoll.automation.engine.backend.api.controllers;

import com.davidrandoll.automation.engine.backend.api.dtos.AllBlockWithSchema;
import com.davidrandoll.automation.engine.backend.api.dtos.BlockType;
import com.davidrandoll.automation.engine.backend.api.dtos.BlocksByType;
import com.davidrandoll.automation.engine.backend.api.services.IAESchemaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AutomationEngineController.class)
@AutoConfigureMockMvc(addFilters = false)
class AutomationEngineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IAESchemaService service;

    @Test
    void getBlocksByType_ShouldReturnActionsWithSchema() throws Exception {
        BlockType actionType = new BlockType("testAction", "Test Action", "Test Description", null, List.of());
        when(service.getBlocksByType("actions", null))
                .thenReturn(new BlocksByType(List.of(actionType)));

        mockMvc.perform(get("/automation-engine/block/actions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(service).getBlocksByType("actions", null);
    }

    @Test
    void getSchemaByBeanName_ShouldReturnSchemaWithProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");
        schema.putObject("properties");
        
        BlockType blockType = new BlockType("testAction", "Test", "Description", schema, List.of());
        when(service.getSchemaByBlockName("testAction"))
                .thenReturn(blockType);

        mockMvc.perform(get("/automation-engine/block/testAction/schema")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("testAction"))
                .andExpect(jsonPath("$.label").value("Test"))
                .andExpect(jsonPath("$.schema.type").value("object"))
                .andExpect(jsonPath("$.schema.properties").exists());
    }

    @Test
    void getAllBlockSchemas_ShouldReturnAllBlockTypes() throws Exception {
        BlockType action = new BlockType("action1", "Action 1", "Desc 1", null, List.of());
        BlockType condition = new BlockType("cond1", "Condition 1", "Desc 2", null, List.of());
        
        when(service.getAllBlockSchemas())
                .thenReturn(new AllBlockWithSchema(List.of(action, condition)));

        mockMvc.perform(get("/automation-engine/blocks/schemas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.types").isArray())
                .andExpect(jsonPath("$.types.length()").value(2))
                .andExpect(jsonPath("$.types[0].name").value("action1"))
                .andExpect(jsonPath("$.types[1].name").value("cond1"));
    }

    @Test
    void getAutomationDefinition_ShouldReturnDefinitionWithSchema() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");
        
        when(service.getAutomationDefinition())
                .thenReturn(new BlockType("AutomationDefinition", "Automation Definition", "The automation definition", schema, List.of()));

        mockMvc.perform(get("/automation-engine/automation-definition/schema")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("AutomationDefinition"))
                .andExpect(jsonPath("$.label").value("Automation Definition"))
                .andExpect(jsonPath("$.schema.type").value("object"));
    }

    @Test
    void getFullAutomationSchema_ShouldReturnValidJsonSchema() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode schema = mapper.createObjectNode();
        schema.put("$schema", "http://json-schema.org/draft-07/schema#");
        schema.put("type", "object");
        
        when(service.getFullAutomationSchema(anyString()))
                .thenReturn(schema);

        mockMvc.perform(get("/automation-engine/schema.json")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(service).getFullAutomationSchema(anyString());
    }
}
