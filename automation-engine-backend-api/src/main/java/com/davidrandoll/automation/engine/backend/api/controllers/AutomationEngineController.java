package com.davidrandoll.automation.engine.backend.api.controllers;

import com.davidrandoll.automation.engine.backend.api.dtos.AllBlockWithSchema;
import com.davidrandoll.automation.engine.backend.api.dtos.BlockType;
import com.davidrandoll.automation.engine.backend.api.dtos.BlocksByType;
import com.davidrandoll.automation.engine.backend.api.services.IAESchemaService;
import com.davidrandoll.automation.engine.creator.AutomationDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/automation-engine")
@RequiredArgsConstructor
public class AutomationEngineController {
    private final IAESchemaService service;

    @GetMapping("block/{type}")
    public BlocksByType getBlocksByType(@PathVariable String type,
                                        @RequestParam(required = false) Boolean includeSchema) {
        return service.getBlocksByType(type, includeSchema);
    }

    @GetMapping("block/{name}/schema")
    public BlockType getSchemaByBeanName(@PathVariable String name) {
        if (AutomationDefinition.class.getSimpleName().equals(name)) {
            return service.getAutomationDefinition();
        }
        return service.getSchemaByBlockName(name);
    }

    @GetMapping("blocks/schemas")
    public AllBlockWithSchema getAllBlockSchemas() {
        return service.getAllBlockSchemas();
    }

    @GetMapping("automation-definition/schema")
    public BlockType getAutomationDefinition() {
        return service.getAutomationDefinition();
    }

    /**
     * Returns a complete JSON schema for automation definitions.
     * This schema can be used to validate automation YAML/JSON files and provides
     * IDE autocomplete support. The schema is dynamically built from all registered
     * triggers, conditions, actions, variables, and results.
     *
     * @return A complete JSON schema for automation definitions
     */
    @GetMapping(value = "schema.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonNode getFullAutomationSchema(HttpServletRequest request) {
        return service.getFullAutomationSchema(request.getRequestURL().toString());
    }
}