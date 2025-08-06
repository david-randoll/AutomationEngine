package com.davidrandoll.automation.engine.backend.api.controllers;

import com.davidrandoll.automation.engine.backend.api.json_schema.JsonSchemaService;
import com.davidrandoll.automation.engine.modules.actions.if_then_else.IfThenElseActionContext;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/automation-engine")
@RequiredArgsConstructor
public class AutomationEngineController {
    private final JsonSchemaService jsonSchemaService;

    @GetMapping
    public JsonNode getAutomationEngineSchema() {
        return jsonSchemaService.generateSchema(IfThenElseActionContext.class);
    }
}