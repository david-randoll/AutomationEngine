package com.davidrandoll.automation.engine.backend.api.controllers;

import com.davidrandoll.automation.engine.backend.api.json_schema.JsonSchemaService;
import com.davidrandoll.automation.engine.spring.modules.actions.uda.IUserDefinedActionRegistry;
import com.davidrandoll.automation.engine.spring.modules.actions.uda.UserDefinedActionDefinition;
import com.davidrandoll.automation.engine.spring.modules.conditions.udc.IUserDefinedConditionRegistry;
import com.davidrandoll.automation.engine.spring.modules.triggers.udt.IUserDefinedTriggerRegistry;
import com.davidrandoll.automation.engine.spring.modules.variables.udv.IUserDefinedVariableRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserDefinedController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserDefinedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IUserDefinedActionRegistry actionRegistry;
    @MockBean
    private IUserDefinedConditionRegistry conditionRegistry;
    @MockBean
    private IUserDefinedTriggerRegistry triggerRegistry;
    @MockBean
    private IUserDefinedVariableRegistry variableRegistry;
    @MockBean
    private JsonSchemaService jsonSchemaService;

    @Test
    void getSchema_ShouldReturnOk_ForActions() throws Exception {
        when(jsonSchemaService.generateSchema(any())).thenReturn(new ObjectMapper().createObjectNode());

        mockMvc.perform(get("/automation-engine/user-defined/actions/schema")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_ShouldReturnOk_ForActions() throws Exception {
        when(actionRegistry.getAllActions()).thenReturn(Map.of());

        mockMvc.perform(get("/automation-engine/user-defined/actions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getSchema_ShouldReturnOk_ForConditions() throws Exception {
        when(jsonSchemaService.generateSchema(any())).thenReturn(new ObjectMapper().createObjectNode());

        mockMvc.perform(get("/automation-engine/user-defined/conditions/schema")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_ShouldReturnOk_ForTriggers() throws Exception {
        when(triggerRegistry.getAllTriggers()).thenReturn(Map.of());

        mockMvc.perform(get("/automation-engine/user-defined/triggers")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void register_ShouldReturnCreated_ForActions() throws Exception {
        String json = "{\"alias\":\"test\"}";

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/automation-engine/user-defined/actions")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void unregister_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .delete("/automation-engine/user-defined/actions/test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void getOne_ShouldReturnOk_WhenFound() throws Exception {
        when(actionRegistry.findAction("test")).thenReturn(Optional.of(new UserDefinedActionDefinition()));

        mockMvc.perform(get("/automation-engine/user-defined/actions/test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getOne_ShouldReturnNotFound_WhenNotFound() throws Exception {
        when(actionRegistry.findAction("test")).thenReturn(Optional.empty());

        mockMvc.perform(get("/automation-engine/user-defined/actions/test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_ShouldReturnCreated() throws Exception {
        String json = "{\"alias\":\"test\"}";

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .put("/automation-engine/user-defined/variables/test")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void invalidBlockType_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/automation-engine/user-defined/invalid/schema")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
