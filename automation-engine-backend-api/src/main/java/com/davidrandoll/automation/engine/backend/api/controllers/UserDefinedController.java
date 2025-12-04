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

import java.util.Map;

@RestController
@RequestMapping("/automation-engine/user-defined")
@RequiredArgsConstructor
public class UserDefinedController {

    private final IUserDefinedActionRegistry actionRegistry;
    private final IUserDefinedConditionRegistry conditionRegistry;
    private final IUserDefinedTriggerRegistry triggerRegistry;
    private final IUserDefinedVariableRegistry variableRegistry;
    private final JsonSchemaService jsonSchemaService;

    private enum BlockEnum {
        ACTIONS, CONDITIONS, TRIGGERS, VARIABLES;

        static BlockEnum from(String s) {
            try {
                return BlockEnum.valueOf(s.toUpperCase());
            } catch (Exception e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid block type: " + s + " (expected: actions, conditions, triggers, variables)"
                );
            }
        }
    }

    @GetMapping("/{blockType}/schema")
    public BlockType getSchema(@PathVariable String blockType) {
        return switch (BlockEnum.from(blockType)) {
            case ACTIONS -> new BlockType(
                    "UserDefinedActionDefinition", "User-Defined Action",
                    "Create a reusable custom action",
                    jsonSchemaService.generateSchema(UserDefinedActionDefinition.class),
                    java.util.List.of()
            );

            case CONDITIONS -> new BlockType(
                    "UserDefinedConditionDefinition", "User-Defined Condition",
                    "Create a reusable custom condition",
                    jsonSchemaService.generateSchema(UserDefinedConditionDefinition.class),
                    java.util.List.of()
            );

            case TRIGGERS -> new BlockType(
                    "UserDefinedTriggerDefinition", "User-Defined Trigger",
                    "Create a reusable custom trigger",
                    jsonSchemaService.generateSchema(UserDefinedTriggerDefinition.class),
                    java.util.List.of()
            );

            case VARIABLES -> new BlockType(
                    "UserDefinedVariableDefinition", "User-Defined Variable",
                    "Create a reusable custom variable",
                    jsonSchemaService.generateSchema(UserDefinedVariableDefinition.class),
                    java.util.List.of()
            );
        };
    }

    @GetMapping("/{blockType}")
    public Map<String, ?> getAll(@PathVariable String blockType) {
        return switch (BlockEnum.from(blockType)) {
            case ACTIONS -> actionRegistry.getAllActions();
            case CONDITIONS -> conditionRegistry.getAllConditions();
            case TRIGGERS -> triggerRegistry.getAllTriggers();
            case VARIABLES -> variableRegistry.getAllVariables();
        };
    }

    @GetMapping("/{blockType}/{name}")
    public ResponseEntity<?> getOne(
            @PathVariable String blockType,
            @PathVariable String name
    ) {
        return switch (BlockEnum.from(blockType)) {
            case ACTIONS -> actionRegistry.findAction(name)
                    .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());

            case CONDITIONS -> conditionRegistry.findCondition(name)
                    .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());

            case TRIGGERS -> triggerRegistry.findTrigger(name)
                    .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());

            case VARIABLES -> variableRegistry.findVariable(name)
                    .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        };
    }

    @PostMapping("/{blockType}")
    public ResponseEntity<?> register(
            @PathVariable String blockType,
            @RequestBody Object definition
    ) {
        return switch (BlockEnum.from(blockType)) {
            case ACTIONS -> {
                var def = (UserDefinedActionDefinition) definition;
                actionRegistry.registerAction(def);
                yield ResponseEntity.status(HttpStatus.CREATED).body(def);
            }
            case CONDITIONS -> {
                var def = (UserDefinedConditionDefinition) definition;
                conditionRegistry.registerCondition(def);
                yield ResponseEntity.status(HttpStatus.CREATED).body(def);
            }
            case TRIGGERS -> {
                var def = (UserDefinedTriggerDefinition) definition;
                triggerRegistry.registerTrigger(def);
                yield ResponseEntity.status(HttpStatus.CREATED).body(def);
            }
            case VARIABLES -> {
                var def = (UserDefinedVariableDefinition) definition;
                variableRegistry.registerVariable(def);
                yield ResponseEntity.status(HttpStatus.CREATED).body(def);
            }
        };
    }

    @DeleteMapping("/{blockType}/{name}")
    public ResponseEntity<Void> unregister(
            @PathVariable String blockType,
            @PathVariable String name
    ) {
        switch (BlockEnum.from(blockType)) {
            case ACTIONS -> actionRegistry.unregisterAction(name);
            case CONDITIONS -> conditionRegistry.unregisterCondition(name);
            case TRIGGERS -> triggerRegistry.unregisterTrigger(name);
            case VARIABLES -> variableRegistry.unregisterVariable(name);
        }
        return ResponseEntity.noContent().build();
    }
}
