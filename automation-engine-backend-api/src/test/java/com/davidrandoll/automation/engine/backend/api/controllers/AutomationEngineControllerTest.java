package com.davidrandoll.automation.engine.backend.api.controllers;

import com.davidrandoll.automation.engine.backend.api.dtos.AllBlockWithSchema;
import com.davidrandoll.automation.engine.backend.api.dtos.BlockType;
import com.davidrandoll.automation.engine.backend.api.dtos.BlocksByType;
import com.davidrandoll.automation.engine.backend.api.services.IAESchemaService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AutomationEngineController.class)
@AutoConfigureMockMvc(addFilters = false)
class AutomationEngineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IAESchemaService service;

    @Test
    void getBlocksByType_ShouldReturnOk() throws Exception {
        when(service.getBlocksByType(anyString(), anyBoolean()))
                .thenReturn(new BlocksByType(List.of()));

        mockMvc.perform(get("/automation-engine/block/actions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getSchemaByBeanName_ShouldReturnOk() throws Exception {
        when(service.getSchemaByBlockName(anyString()))
                .thenReturn(new BlockType("test", "Test", "Desc", null, List.of()));

        mockMvc.perform(get("/automation-engine/block/testAction/schema")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getAllBlockSchemas_ShouldReturnOk() throws Exception {
        when(service.getAllBlockSchemas())
                .thenReturn(new AllBlockWithSchema(List.of()));

        mockMvc.perform(get("/automation-engine/blocks/schemas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getAutomationDefinition_ShouldReturnOk() throws Exception {
        when(service.getAutomationDefinition())
                .thenReturn(new BlockType("def", "Def", "Desc", null, List.of()));

        mockMvc.perform(get("/automation-engine/automation-definition/schema")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getFullAutomationSchema_ShouldReturnOk() throws Exception {
        when(service.getFullAutomationSchema(anyString()))
                .thenReturn(new ObjectMapper().createObjectNode());

        mockMvc.perform(get("/automation-engine/schema.json")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
