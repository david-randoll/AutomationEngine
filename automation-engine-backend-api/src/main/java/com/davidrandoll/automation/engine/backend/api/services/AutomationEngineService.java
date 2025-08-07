package com.davidrandoll.automation.engine.backend.api.services;

import com.davidrandoll.automation.engine.backend.api.dtos.AllModuleWithSchema;
import com.davidrandoll.automation.engine.backend.api.dtos.ModuleType;
import com.davidrandoll.automation.engine.backend.api.dtos.ModulesByType;
import com.davidrandoll.automation.engine.backend.api.json_schema.JsonSchemaService;
import com.davidrandoll.automation.engine.core.IModule;
import com.davidrandoll.automation.engine.core.actions.IAction;
import com.davidrandoll.automation.engine.core.conditions.ICondition;
import com.davidrandoll.automation.engine.core.result.IResult;
import com.davidrandoll.automation.engine.core.triggers.ITrigger;
import com.davidrandoll.automation.engine.core.variables.IVariable;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AutomationEngineService {
    private final JsonSchemaService jsonSchemaService;
    private final ApplicationContext application;

    public ModulesByType getModulesByType(@PathVariable String moduleType, Boolean includeSchema) {
        Class<? extends IModule> clazz = getBeanByModule(moduleType);
        List<ModuleType> types = getModuleByType(clazz, includeSchema);
        return new ModulesByType(types);
    }

    public ModuleType getSchemaByModuleName(@PathVariable String name) {
        Object bean = application.getBean(name);
        if (!(bean instanceof IModule module)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "%s is not a valid module".formatted(name));
        }
        var schema = jsonSchemaService.generateSchema(module.getContextType());
        return new ModuleType(name, module, schema);
    }

    public AllModuleWithSchema getAllModuleSchemas() {
        List<ModuleType> types = getModuleByType(IModule.class, true);
        return new AllModuleWithSchema(types);
    }

    private List<ModuleType> getModuleByType(Class<? extends IModule> clazz, Boolean includeSchema) {
        Map<String, ? extends IModule> beans = application.getBeansOfType(clazz);
        return beans.entrySet().stream()
                .map(entry -> {
                    IModule module = entry.getValue();
                    if (includeSchema == null || Boolean.FALSE.equals(includeSchema)) {
                        return new ModuleType(entry.getKey(), module, null);
                    }
                    var schema = jsonSchemaService.generateSchema(module.getContextType());
                    return new ModuleType(entry.getKey(), module, schema);
                })
                .toList();
    }

    private static Class<? extends IModule> getBeanByModule(String type) {
        return switch (type.toLowerCase()) {
            case "action", "actions" -> IAction.class;
            case "condition", "conditions" -> ICondition.class;
            case "trigger", "triggers" -> ITrigger.class;
            case "result", "results" -> IResult.class;
            case "variable", "variables" -> IVariable.class;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid type: " + type);
        };
    }
}
