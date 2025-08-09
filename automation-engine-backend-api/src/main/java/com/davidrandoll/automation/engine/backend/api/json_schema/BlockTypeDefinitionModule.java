package com.davidrandoll.automation.engine.backend.api.json_schema;

import com.davidrandoll.automation.engine.creator.actions.ActionDefinition;
import com.davidrandoll.automation.engine.creator.conditions.ConditionDefinition;
import com.davidrandoll.automation.engine.creator.triggers.TriggerDefinition;
import com.davidrandoll.automation.engine.creator.variables.VariableDefinition;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;

public class BlockTypeDefinitionModule implements Module {
    @Override
    public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
        builder.forTypesInGeneral()
                .withTypeAttributeOverride((attrs, type, ctx) -> {
                    String blockType = getBlockType(type.getType().getErasedType());
                    if (blockType != null) {
                        attrs.put("x-block-type", blockType);
                    }
                });
        builder.forFields()
                .withInstanceAttributeOverride((attrs, type, context) -> {
                    String blockType = getBlockType(type.getType().getErasedType());
                    if (blockType != null) {
                        attrs.put("x-block-type", blockType);
                    }
                });
    }

    private String getBlockType(Class<?> type) {
        if (TriggerDefinition.class.isAssignableFrom(type)) return "trigger";
        if (ConditionDefinition.class.isAssignableFrom(type)) return "condition";
        if (ActionDefinition.class.isAssignableFrom(type)) return "action";
        if (VariableDefinition.class.isAssignableFrom(type)) return "variable";
        return null;
    }
}


