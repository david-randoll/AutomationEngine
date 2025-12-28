package com.davidrandoll.automation.engine.spring.modules.variables.if_them_else;

import com.davidrandoll.automation.engine.core.variables.IVariableContext;
import com.davidrandoll.automation.engine.creator.conditions.ConditionDefinition;
import com.davidrandoll.automation.engine.creator.variables.VariableDefinition;
import com.davidrandoll.automation.engine.spring.spi.ContextField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldNameConstants
@JsonPropertyOrder({
        IfThenElseVariableContext.Fields.alias,
        IfThenElseVariableContext.Fields.description,
        IfThenElseVariableContext.Fields.ifConditions,
        IfThenElseVariableContext.Fields.thenVariables,
        IfThenElseVariableContext.Fields.ifThenBlocks,
        IfThenElseVariableContext.Fields.elseVariable
})
public class IfThenElseVariableContext implements IVariableContext {
    private String alias;
    private String description;

    @ContextField(
        helpText = "Primary conditions to evaluate. ALL must be true to set 'then' variables"
    )
    @JsonProperty("if")
    private List<ConditionDefinition> ifConditions = new ArrayList<>();

    @ContextField(
        helpText = "Variables to set when ALL 'if' conditions are true"
    )
    @JsonProperty("then")
    private List<VariableDefinition> thenVariables = new ArrayList<>();

    @ContextField(
        helpText = "Additional else-if blocks evaluated in order when primary 'if' fails"
    )
    @JsonProperty("ifs")
    private List<IfThenBlock> ifThenBlocks = new ArrayList<>();

    @ContextField(
        helpText = "Fallback variables when no if/else-if conditions match"
    )
    @JsonProperty("else")
    private List<VariableDefinition> elseVariable = new ArrayList<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldNameConstants
    @JsonPropertyOrder({
            IfThenBlock.Fields.alias,
            IfThenBlock.Fields.description,
            IfThenBlock.Fields.ifConditions,
            IfThenBlock.Fields.thenVariables
    })
    public static class IfThenBlock implements IVariableContext {
        private String alias;
        private String description;

        @ContextField(
            helpText = "Conditions for this else-if block. ALL must be true"
        )
        @JsonProperty("if")
        private List<ConditionDefinition> ifConditions = new ArrayList<>();

        @ContextField(
            helpText = "Variables to set when this block's conditions are met"
        )
        @JsonProperty("then")
        private List<VariableDefinition> thenVariables = new ArrayList<>();
    }
}