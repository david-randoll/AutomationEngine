package com.davidrandoll.automation.engine.backend.api.services;

import com.davidrandoll.automation.engine.backend.api.dtos.AllBlockWithSchema;
import com.davidrandoll.automation.engine.backend.api.dtos.BlockType;
import com.davidrandoll.automation.engine.backend.api.dtos.BlocksByType;
import com.davidrandoll.automation.engine.backend.api.json_schema.JsonSchemaService;
import com.davidrandoll.automation.engine.core.IBlock;
import com.davidrandoll.automation.engine.core.actions.IAction;
import com.davidrandoll.automation.engine.core.conditions.ICondition;
import com.davidrandoll.automation.engine.core.result.IResult;
import com.davidrandoll.automation.engine.core.triggers.ITrigger;
import com.davidrandoll.automation.engine.core.variables.IVariable;
import com.davidrandoll.automation.engine.creator.AutomationDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class AESchemaService implements IAESchemaService {
    private final JsonSchemaService jsonSchemaService;
    private final ApplicationContext application;

    @Override
    public BlockType getAutomationDefinition() {
        var schema = jsonSchemaService.generateSchema(AutomationDefinition.class);
        return new BlockType(
                AutomationDefinition.class.getSimpleName(),
                "Automation Definition",
                "The parent block for all automations",
                schema
        );
    }

    @Override
    public BlocksByType getBlocksByType(String moduleType, Boolean includeSchema) {
        Class<? extends IBlock> clazz = getBeanByModule(moduleType);
        List<BlockType> types = getModuleByType(clazz, includeSchema);
        return new BlocksByType(types);
    }

    @Override
    public BlockType getSchemaByBlockName(String name) {
        //check if bean exists
        if (!application.containsBean(name)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No module found with name: " + name);
        }

        Object bean = application.getBean(name);
        if (!(bean instanceof IBlock module)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "%s is not a valid module".formatted(name));
        }
        var schema = jsonSchemaService.generateSchema(module.getContextType());
        return new BlockType(name, module, schema);
    }

    @Override
    public AllBlockWithSchema getAllBlockSchemas() {
        List<BlockType> types = getModuleByType(IBlock.class, true);
        return new AllBlockWithSchema(types);
    }

    private List<BlockType> getModuleByType(Class<? extends IBlock> clazz, Boolean includeSchema) {
        Map<String, ? extends IBlock> beans = application.getBeansOfType(clazz);
        return beans.entrySet().stream()
                .map(entry -> {
                    IBlock module = entry.getValue();
                    if (includeSchema == null || Boolean.FALSE.equals(includeSchema)) {
                        return new BlockType(entry.getKey(), module, null);
                    }
                    var schema = jsonSchemaService.generateSchema(module.getContextType());
                    return new BlockType(entry.getKey(), module, schema);
                })
                .toList();
    }

    private static Class<? extends IBlock> getBeanByModule(String type) {
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
