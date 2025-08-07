package com.davidrandoll.automation.engine.backend.api.controllers;

import com.davidrandoll.automation.engine.backend.api.dtos.AllModuleWithSchema;
import com.davidrandoll.automation.engine.backend.api.dtos.ModuleType;
import com.davidrandoll.automation.engine.backend.api.dtos.ModulesByType;
import com.davidrandoll.automation.engine.backend.api.services.AutomationEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/automation-engine")
@RequiredArgsConstructor
public class AutomationEngineController {
    private final AutomationEngineService service;

    @GetMapping("module/{type}")
    public ModulesByType getModulesByType(@PathVariable String type,
                                          @RequestParam(required = false) Boolean includeSchema) {
        return service.getModulesByType(type, includeSchema);
    }

    @GetMapping("module/{name}/schema")
    public ModuleType getSchemaByBeanName(@PathVariable String name) {
        return service.getSchemaByModuleName(name);
    }

    @GetMapping("modules/schemas")
    public AllModuleWithSchema getAllModuleSchemas() {
        return service.getAllModuleSchemas();
    }
}