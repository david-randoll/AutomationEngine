package com.davidrandoll.automation.engine.backend.api.services;

import com.davidrandoll.automation.engine.backend.api.dtos.BlockType;
import com.davidrandoll.automation.engine.backend.api.json_schema.JsonSchemaService;
import com.davidrandoll.automation.engine.core.IBlock;
import com.davidrandoll.automation.engine.core.actions.IAction;
import com.davidrandoll.automation.engine.core.conditions.ICondition;
import com.davidrandoll.automation.engine.core.result.IResult;
import com.davidrandoll.automation.engine.core.triggers.ITrigger;
import com.davidrandoll.automation.engine.core.variables.IVariable;
import com.davidrandoll.automation.engine.creator.AutomationDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AESchemaServiceTest {

    @Mock
    private JsonSchemaService jsonSchemaService;
    @Mock
    private ApplicationContext applicationContext;

    private AESchemaService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        service = new AESchemaService(jsonSchemaService, applicationContext);
    }

    @Test
    void getAutomationDefinition_ShouldReturnCorrectBlockType() {
        ObjectNode schema = objectMapper.createObjectNode();
        when(jsonSchemaService.generateSchema(AutomationDefinition.class)).thenReturn(schema);

        BlockType result = service.getAutomationDefinition();

        assertThat(result.getName()).isEqualTo("AutomationDefinition");
        assertThat(result.getLabel()).isEqualTo("Automation Definition");
        assertThat(result.getSchema()).isEqualTo(schema);
    }

    @Test
    void getBlocksByType_ShouldReturnBlocks_WhenTypeIsAction() {
        IAction action = mock(IAction.class);
        when(action.getContextType()).thenAnswer(inv -> Object.class);
        when(applicationContext.getBeansOfType(IAction.class)).thenReturn(Map.of("testAction", action));

        ObjectNode schema = objectMapper.createObjectNode();
        when(jsonSchemaService.generateSchema(any())).thenReturn(schema);

        var result = service.getBlocksByType("actions", true);

        assertThat(result.getTypes()).hasSize(1);
        assertThat(result.getTypes().get(0).getName()).isEqualTo("testAction");
        assertThat(result.getTypes().get(0).getSchema()).isEqualTo(schema);
    }

    @Test
    void getSchemaByBlockName_ShouldReturnSchema_WhenBeanExists() {
        IAction action = mock(IAction.class);
        when(action.getContextType()).thenAnswer(inv -> Object.class);
        when(applicationContext.containsBean("testAction")).thenReturn(true);
        when(applicationContext.getBean("testAction")).thenReturn(action);

        ObjectNode schema = objectMapper.createObjectNode();
        when(jsonSchemaService.generateSchema(any())).thenReturn(schema);

        BlockType result = service.getSchemaByBlockName("testAction");

        assertThat(result.getName()).isEqualTo("testAction");
        assertThat(result.getSchema()).isEqualTo(schema);
    }

    @Test
    void getSchemaByBlockName_ShouldThrowNotFound_WhenBeanDoesNotExist() {
        when(applicationContext.containsBean("unknown")).thenReturn(false);

        assertThatThrownBy(() -> service.getSchemaByBlockName("unknown"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No module found");
    }

    @Test
    void getBlocksByType_ShouldReturnTriggers() {
        ITrigger trigger = mock(ITrigger.class);
        when(applicationContext.getBeansOfType(ITrigger.class)).thenReturn(Map.of("testTrigger", trigger));

        var result = service.getBlocksByType("triggers", false);
        assertThat(result.getTypes()).hasSize(1);
        assertThat(result.getTypes().get(0).getName()).isEqualTo("testTrigger");
    }

    @Test
    void getSchemaByBlockName_ShouldThrow_WhenBeanIsNotIBlock() {
        when(applicationContext.containsBean("notBlock")).thenReturn(true);
        when(applicationContext.getBean("notBlock")).thenReturn(new Object());

        assertThatThrownBy(() -> service.getSchemaByBlockName("notBlock"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not a valid module");
    }

    @Test
    void getFullAutomationSchema_ShouldReturnSchema_WithBlocks() {
        // Setup trigger schema (generic fallback)
        ObjectNode triggerSchema = objectMapper.createObjectNode();
        when(jsonSchemaService.generateSchema(any())).thenReturn(triggerSchema);

        // Setup root schema
        ObjectNode rootSchema = objectMapper.createObjectNode();
        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("triggers", objectMapper.createObjectNode());
        rootSchema.set("properties", properties);
        when(jsonSchemaService.generateSchema(eq(AutomationDefinition.class))).thenReturn(rootSchema);

        // Setup trigger bean
        ITrigger trigger = mock(ITrigger.class);
        when(trigger.getContextType()).thenAnswer(inv -> Object.class);
        when(applicationContext.getBeansOfType(ITrigger.class)).thenReturn(Map.of("myTrigger", trigger));

        var result = service.getFullAutomationSchema("http://localhost");

        assertThat(result).isNotNull();
        // Verify that definitions were created - now using fully qualified names
        assertThat(result.has("$defs")).isTrue();
        assertThat(result.get("$defs").has("com.davidrandoll.automation.engine.creator.triggers.TriggerDefinition")).isTrue();
    }

    @Test
    void getAllBlockSchemas_ShouldReturnAll() {
        IAction action = mock(IAction.class);
        when(action.getContextType()).thenAnswer(inv -> Object.class);
        when(applicationContext.getBeansOfType(IBlock.class)).thenReturn(Map.of("testAction", action));
        when(jsonSchemaService.generateSchema(any())).thenReturn(objectMapper.createObjectNode());

        var result = service.getAllBlockSchemas();
        assertThat(result.getTypes()).hasSize(1);
    }

    @Test
    void getFullAutomationSchema_ShouldHandleComplexSchemaStructures() {
        // Setup root schema
        ObjectNode rootSchema = objectMapper.createObjectNode();
        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("triggers", objectMapper.createObjectNode());
        rootSchema.set("properties", properties);
        when(jsonSchemaService.generateSchema(eq(AutomationDefinition.class))).thenReturn(rootSchema);

        // Setup trigger with complex schema
        ITrigger trigger = mock(ITrigger.class);
        when(trigger.getContextType()).thenAnswer(inv -> Object.class);
        when(applicationContext.getBeansOfType(ITrigger.class)).thenReturn(Map.of("complexTrigger", trigger));

        ObjectNode triggerSchema = objectMapper.createObjectNode();
        ObjectNode triggerProps = objectMapper.createObjectNode();
        triggerProps.set("simpleField", objectMapper.createObjectNode().put("type", "string"));

        // Add nested ref
        ObjectNode refField = objectMapper.createObjectNode();
        refField.put("$ref", "#/$defs/ComplexType");
        triggerProps.set("refField", refField);

        triggerSchema.set("properties", triggerProps);

        // Add defs
        ObjectNode defs = objectMapper.createObjectNode();
        ObjectNode complexType = objectMapper.createObjectNode();
        complexType.put("type", "object");
        defs.set("ComplexType", complexType);
        triggerSchema.set("$defs", defs);

        // Add additionalProperties as boolean
        triggerSchema.put("additionalProperties", true);

        when(jsonSchemaService.generateSchema(eq(Object.class))).thenReturn(triggerSchema);

        var result = service.getFullAutomationSchema("http://localhost");

        assertThat(result).isNotNull();
        assertThat(result.has("$defs")).isTrue();
        // Check if ComplexType was moved to global defs
        assertThat(result.get("$defs").has("ComplexType")).isTrue();
    }

    @Test
    void getFullAutomationSchema_ShouldHandleBlockWithoutSuffix() {
        // Setup root schema
        ObjectNode rootSchema = objectMapper.createObjectNode();
        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("actions", objectMapper.createObjectNode());
        rootSchema.set("properties", properties);
        when(jsonSchemaService.generateSchema(eq(AutomationDefinition.class))).thenReturn(rootSchema);

        // Setup action with name not ending in "Action"
        IAction action = mock(IAction.class);
        when(action.getContextType()).thenAnswer(inv -> Object.class);
        when(applicationContext.getBeansOfType(IAction.class)).thenReturn(Map.of("customTask", action));

        ObjectNode actionSchema = objectMapper.createObjectNode();
        when(jsonSchemaService.generateSchema(eq(Object.class))).thenReturn(actionSchema);

        var result = service.getFullAutomationSchema("http://localhost");

        assertThat(result).isNotNull();
        // Now using fully qualified name for ActionDefinition
        JsonNode actionDef = result.get("$defs").get("com.davidrandoll.automation.engine.creator.actions.ActionDefinition");
        JsonNode enumValues = actionDef.get("allOf").get(0).get("properties").get("action").get("enum");

        // Should use full name if suffix not found
        boolean found = false;
        for (JsonNode val : enumValues) {
            if (val.asText().equals("customTask")) {
                found = true;
                break;
            }
        }
        assertThat(found).isTrue();
    }

    @Test
    void getFullAutomationSchema_ShouldHandleSchemaWithArrayAndAdditionalPropsObject() {
        // Setup root schema
        ObjectNode rootSchema = objectMapper.createObjectNode();
        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("conditions", objectMapper.createObjectNode());
        rootSchema.set("properties", properties);
        when(jsonSchemaService.generateSchema(eq(AutomationDefinition.class))).thenReturn(rootSchema);

        // Setup condition
        ICondition condition = mock(ICondition.class);
        when(condition.getContextType()).thenAnswer(inv -> Object.class);
        when(applicationContext.getBeansOfType(ICondition.class)).thenReturn(Map.of("listCondition", condition));

        ObjectNode conditionSchema = objectMapper.createObjectNode();
        ObjectNode props = objectMapper.createObjectNode();

        // Array field
        ObjectNode arrayField = objectMapper.createObjectNode();
        arrayField.put("type", "array");
        arrayField.set("items", objectMapper.createObjectNode().put("type", "string"));
        props.set("tags", arrayField);

        conditionSchema.set("properties", props);

        // additionalProperties as object
        ObjectNode addProps = objectMapper.createObjectNode();
        addProps.put("type", "integer");
        conditionSchema.set("additionalProperties", addProps);

        when(jsonSchemaService.generateSchema(eq(Object.class))).thenReturn(conditionSchema);

        var result = service.getFullAutomationSchema("http://localhost");

        assertThat(result).isNotNull();
        // Now using fully qualified name for ConditionDefinition
        JsonNode conditionDef = result.get("$defs").get("com.davidrandoll.automation.engine.creator.conditions.ConditionDefinition");
        // Verify structure
        assertThat(conditionDef).isNotNull();
    }

    @Test
    void getFullAutomationSchema_ShouldHandleAllOfAndItems() {
        // Setup root schema
        ObjectNode rootSchema = objectMapper.createObjectNode();
        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("variables", objectMapper.createObjectNode());
        rootSchema.set("properties", properties);
        when(jsonSchemaService.generateSchema(eq(AutomationDefinition.class))).thenReturn(rootSchema);

        // Setup variable
        IVariable variable = mock(IVariable.class);
        when(variable.getContextType()).thenAnswer(inv -> Object.class);
        when(applicationContext.getBeansOfType(IVariable.class)).thenReturn(Map.of("complexVar", variable));

        ObjectNode varSchema = objectMapper.createObjectNode();
        ObjectNode props = objectMapper.createObjectNode();

        // Field with allOf
        ObjectNode allOfField = objectMapper.createObjectNode();
        ArrayNode allOfArray = objectMapper.createArrayNode();
        allOfArray.add(objectMapper.createObjectNode().put("type", "string"));
        allOfArray.add(objectMapper.createObjectNode().put("minLength", 1));
        allOfField.set("allOf", allOfArray);
        props.set("compositeField", allOfField);

        varSchema.set("properties", props);
        when(jsonSchemaService.generateSchema(eq(Object.class))).thenReturn(varSchema);

        var result = service.getFullAutomationSchema("http://localhost");

        assertThat(result).isNotNull();
        // Now using fully qualified name for VariableDefinition
        JsonNode varDef = result.get("$defs").get("com.davidrandoll.automation.engine.creator.variables.VariableDefinition");
        assertThat(varDef).isNotNull();
    }
}
