package com.davidrandoll.automation.engine.backend.api.controllers;

import com.davidrandoll.automation.engine.backend.api.dtos.AllBlockWithSchema;
import com.davidrandoll.automation.engine.backend.api.dtos.BlockType;
import com.davidrandoll.automation.engine.backend.api.dtos.BlocksByType;
import com.davidrandoll.automation.engine.backend.api.services.AESchemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/automation-engine")
@RequiredArgsConstructor
public class AutomationEngineController {
    private final AESchemaService service;

    @GetMapping("block/{type}")
    public BlocksByType getBlocksByType(@PathVariable String type,
                                        @RequestParam(required = false) Boolean includeSchema) {
        return service.getBlocksByType(type, includeSchema);
    }

    @GetMapping("block/{name}/schema")
    public BlockType getSchemaByBeanName(@PathVariable String name) {
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
}