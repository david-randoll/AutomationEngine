package com.davidrandoll.automation.engine.backend.api.controllers;

import com.davidrandoll.automation.engine.backend.api.json_schema.JsonSchemaService;
import com.davidrandoll.automation.engine.spring.modules.actions.uda.IUserDefinedActionRegistry;
import com.davidrandoll.automation.engine.spring.modules.actions.uda.UserDefinedActionDefinition;
import com.davidrandoll.automation.engine.spring.modules.conditions.udc.IUserDefinedConditionRegistry;
import com.davidrandoll.automation.engine.spring.modules.triggers.udt.IUserDefinedTriggerRegistry;
import com.davidrandoll.automation.engine.spring.modules.variables.udv.IUserDefinedVariableRegistry;
import com.davidrandoll.automation.engine.spring.modules.variables.udv.UserDefinedVariableDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    void getSchema_ShouldReturnSchemaWithTypeObject_ForActions() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");
        
        when(jsonSchemaService.generateSchema(any())).thenReturn(schema);

        mockMvc.perform(get("/automation-engine/user-defined/actions/schema")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schema.type").value("object"));
    }

    @Test
    void getAll_ShouldReturnEmptyMap_ForActions() throws Exception {
        when(actionRegistry.getAllActions()).thenReturn(Map.of());

        mockMvc.perform(get("/automation-engine/user-defined/actions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isMap());
    }

    @Test
    void getSchema_ShouldReturnSchemaWithTypeObject_ForConditions() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");
        
        when(jsonSchemaService.generateSchema(any())).thenReturn(schema);

        mockMvc.perform(get("/automation-engine/user-defined/conditions/schema")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schema.type").value("object"));
    }

    @Test
    void getAll_ShouldReturnEmptyMap_ForTriggers() throws Exception {
        when(triggerRegistry.getAllTriggers()).thenReturn(Map.of());

        mockMvc.perform(get("/automation-engine/user-defined/triggers")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isMap());
    }

    @Test
    void register_ShouldReturnCreated_ForActions() throws Exception {
        String json = "{\"alias\":\"test\",\"description\":\"Test action\"}";

        mockMvc.perform(post("/automation-engine/user-defined/actions")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        
        verify(actionRegistry).registerAction(any(UserDefinedActionDefinition.class));
    }

    @Test
    void unregister_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/automation-engine/user-defined/actions/test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        
        verify(actionRegistry).unregisterAction("test");
    }

    @Test
    void getOne_ShouldReturnActionWithAlias_WhenFound() throws Exception {
        UserDefinedActionDefinition action = new UserDefinedActionDefinition();
        action.setAlias("test");
        action.setDescription("Test description");
        
        when(actionRegistry.findAction("test")).thenReturn(Optional.of(action));

        mockMvc.perform(get("/automation-engine/user-defined/actions/test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alias").value("test"))
                .andExpect(jsonPath("$.description").value("Test description"));
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
        String json = "{\"alias\":\"test\",\"description\":\"Updated variable\"}";

        mockMvc.perform(put("/automation-engine/user-defined/variables/test")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        
        verify(variableRegistry).registerVariable(any(UserDefinedVariableDefinition.class));
    }

    @Test
    void invalidBlockType_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/automation-engine/user-defined/invalid/schema")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
