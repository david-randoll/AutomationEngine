package com.davidrandoll.automation.engine.backend.api.controllers;

import com.davidrandoll.automation.engine.backend.api.dtos.BlockType;
import com.davidrandoll.automation.engine.backend.api.json_schema.JsonSchemaService;
import com.davidrandoll.automation.engine.spring.modules.actions.uda.IUserDefinedActionRegistry;
import com.davidrandoll.automation.engine.spring.modules.actions.uda.UserDefinedActionDefinition;
import com.davidrandoll.automation.engine.spring.modules.conditions.udc.IUserDefinedConditionRegistry;
import com.davidrandoll.automation.engine.spring.modules.conditions.udc.UserDefinedConditionDefinition;
import com.davidrandoll.automation.engine.spring.modules.triggers.udt.IUserDefinedTriggerRegistry;
import com.davidrandoll.automation.engine.spring.modules.triggers.udt.UserDefinedTriggerDefinition;
import com.davidrandoll.automation.engine.spring.modules.variables.udv.IUserDefinedVariableRegistry;
import com.davidrandoll.automation.engine.spring.modules.variables.udv.UserDefinedVariableDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * REST controller for managing user-defined block types (actions, conditions, triggers, variables).
 * Provides endpoints for registering, unregistering, listing, and retrieving user-defined definitions.
 */
@RestController
@RequestMapping("/automation-engine/user-defined")
@RequiredArgsConstructor
public class UserDefinedController {

    private final IUserDefinedActionRegistry actionRegistry;
    private final IUserDefinedConditionRegistry conditionRegistry;
    private final IUserDefinedTriggerRegistry triggerRegistry;
    private final IUserDefinedVariableRegistry variableRegistry;
    private final JsonSchemaService jsonSchemaService;

    // ==================== Schema ====================

    /**
     * Get the JSON schema for a specific user-defined block type.
     *
     * @param blockType the block type (actions, conditions, triggers, variables)
     * @return the schema for the block type
     */
    @GetMapping("/{blockType}/schema")
    public BlockType getSchema(@PathVariable String blockType) {
        return switch (blockType.toLowerCase()) {
            case "actions" -> new BlockType(
                    "UserDefinedActionDefinition",
                    "User-Defined Action",
                    "Create a reusable custom action with parameters",
                    jsonSchemaService.generateSchema(UserDefinedActionDefinition.class),
                    List.of());
            case "conditions" -> new BlockType(
                    "UserDefinedConditionDefinition",
                    "User-Defined Condition",
                    "Create a reusable custom condition with parameters",
                    jsonSchemaService.generateSchema(UserDefinedConditionDefinition.class),
                    List.of());
            case "triggers" -> new BlockType(
                    "UserDefinedTriggerDefinition",
                    "User-Defined Trigger",
                    "Create a reusable custom trigger with parameters",
                    jsonSchemaService.generateSchema(UserDefinedTriggerDefinition.class),
                    List.of());
            case "variables" -> new BlockType(
                    "UserDefinedVariableDefinition",
                    "User-Defined Variable",
                    "Create a reusable custom variable with parameters",
                    jsonSchemaService.generateSchema(UserDefinedVariableDefinition.class),
                    List.of());
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid block type: " + blockType);
        };
    }

    // ==================== Actions ====================

    /**
     * Get all registered user-defined actions.
     *
     * @return a map of all registered actions (name -> definition)
     */
    @GetMapping("/actions")
    public Map<String, UserDefinedActionDefinition> getAllActions() {
        return actionRegistry.getAllActions();
    }

    /**
     * Get a specific user-defined action by name.
     *
     * @param name the name of the action
     * @return the action definition if found, 404 otherwise
     */
    @GetMapping("/actions/{name}")
    public ResponseEntity<UserDefinedActionDefinition> getAction(@PathVariable String name) {
        return actionRegistry.findAction(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Register a new user-defined action.
     *
     * @param definition the action definition to register
     * @return 201 Created with the registered definition
     */
    @PostMapping("/actions")
    public ResponseEntity<UserDefinedActionDefinition> registerAction(@RequestBody UserDefinedActionDefinition definition) {
        actionRegistry.registerAction(definition);
        return ResponseEntity.status(HttpStatus.CREATED).body(definition);
    }

    /**
     * Unregister a user-defined action.
     *
     * @param name the name of the action to unregister
     * @return 204 No Content
     */
    @DeleteMapping("/actions/{name}")
    public ResponseEntity<Void> unregisterAction(@PathVariable String name) {
        actionRegistry.unregisterAction(name);
        return ResponseEntity.noContent().build();
    }

    // ==================== Conditions ====================

    /**
     * Get all registered user-defined conditions.
     *
     * @return a map of all registered conditions (name -> definition)
     */
    @GetMapping("/conditions")
    public Map<String, UserDefinedConditionDefinition> getAllConditions() {
        return conditionRegistry.getAllConditions();
    }

    /**
     * Get a specific user-defined condition by name.
     *
     * @param name the name of the condition
     * @return the condition definition if found, 404 otherwise
     */
    @GetMapping("/conditions/{name}")
    public ResponseEntity<UserDefinedConditionDefinition> getCondition(@PathVariable String name) {
        return conditionRegistry.findCondition(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Register a new user-defined condition.
     *
     * @param definition the condition definition to register
     * @return 201 Created with the registered definition
     */
    @PostMapping("/conditions")
    public ResponseEntity<UserDefinedConditionDefinition> registerCondition(@RequestBody UserDefinedConditionDefinition definition) {
        conditionRegistry.registerCondition(definition);
        return ResponseEntity.status(HttpStatus.CREATED).body(definition);
    }

    /**
     * Unregister a user-defined condition.
     *
     * @param name the name of the condition to unregister
     * @return 204 No Content
     */
    @DeleteMapping("/conditions/{name}")
    public ResponseEntity<Void> unregisterCondition(@PathVariable String name) {
        conditionRegistry.unregisterCondition(name);
        return ResponseEntity.noContent().build();
    }

    // ==================== Triggers ====================

    /**
     * Get all registered user-defined triggers.
     *
     * @return a map of all registered triggers (name -> definition)
     */
    @GetMapping("/triggers")
    public Map<String, UserDefinedTriggerDefinition> getAllTriggers() {
        return triggerRegistry.getAllTriggers();
    }

    /**
     * Get a specific user-defined trigger by name.
     *
     * @param name the name of the trigger
     * @return the trigger definition if found, 404 otherwise
     */
    @GetMapping("/triggers/{name}")
    public ResponseEntity<UserDefinedTriggerDefinition> getTrigger(@PathVariable String name) {
        return triggerRegistry.findTrigger(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Register a new user-defined trigger.
     *
     * @param definition the trigger definition to register
     * @return 201 Created with the registered definition
     */
    @PostMapping("/triggers")
    public ResponseEntity<UserDefinedTriggerDefinition> registerTrigger(@RequestBody UserDefinedTriggerDefinition definition) {
        triggerRegistry.registerTrigger(definition);
        return ResponseEntity.status(HttpStatus.CREATED).body(definition);
    }

    /**
     * Unregister a user-defined trigger.
     *
     * @param name the name of the trigger to unregister
     * @return 204 No Content
     */
    @DeleteMapping("/triggers/{name}")
    public ResponseEntity<Void> unregisterTrigger(@PathVariable String name) {
        triggerRegistry.unregisterTrigger(name);
        return ResponseEntity.noContent().build();
    }

    // ==================== Variables ====================

    /**
     * Get all registered user-defined variables.
     *
     * @return a map of all registered variables (name -> definition)
     */
    @GetMapping("/variables")
    public Map<String, UserDefinedVariableDefinition> getAllVariables() {
        return variableRegistry.getAllVariables();
    }

    /**
     * Get a specific user-defined variable by name.
     *
     * @param name the name of the variable
     * @return the variable definition if found, 404 otherwise
     */
    @GetMapping("/variables/{name}")
    public ResponseEntity<UserDefinedVariableDefinition> getVariable(@PathVariable String name) {
        return variableRegistry.findVariable(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Register a new user-defined variable.
     *
     * @param definition the variable definition to register
     * @return 201 Created with the registered definition
     */
    @PostMapping("/variables")
    public ResponseEntity<UserDefinedVariableDefinition> registerVariable(@RequestBody UserDefinedVariableDefinition definition) {
        variableRegistry.registerVariable(definition);
        return ResponseEntity.status(HttpStatus.CREATED).body(definition);
    }

    /**
     * Unregister a user-defined variable.
     *
     * @param name the name of the variable to unregister
     * @return 204 No Content
     */
    @DeleteMapping("/variables/{name}")
    public ResponseEntity<Void> unregisterVariable(@PathVariable String name) {
        variableRegistry.unregisterVariable(name);
        return ResponseEntity.noContent().build();
    }
}
