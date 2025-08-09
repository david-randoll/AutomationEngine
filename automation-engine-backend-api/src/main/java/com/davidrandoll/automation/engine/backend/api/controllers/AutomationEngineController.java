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

    @GetMapping("module/{type}")
    public BlocksByType getModulesByType(@PathVariable String type,
                                         @RequestParam(required = false) Boolean includeSchema) {
        return service.getModulesByType(type, includeSchema);
    }

    @GetMapping("module/{name}/schema")
    public BlockType getSchemaByBeanName(@PathVariable String name) {
        return service.getSchemaByModuleName(name);
    }

    @GetMapping("modules/schemas")
    public AllBlockWithSchema getAllModuleSchemas() {
        return service.getAllModuleSchemas();
    }
}