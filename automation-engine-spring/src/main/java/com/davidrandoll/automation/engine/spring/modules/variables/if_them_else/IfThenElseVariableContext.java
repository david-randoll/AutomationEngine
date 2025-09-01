package com.davidrandoll.automation.engine.spring.modules.variables.if_them_else;

import com.davidrandoll.automation.engine.core.variables.IVariableContext;
import com.davidrandoll.automation.engine.creator.conditions.ConditionDefinition;
import com.davidrandoll.automation.engine.creator.variables.VariableDefinition;
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

    @JsonProperty("if")
    private List<ConditionDefinition> ifConditions = new ArrayList<>();

    @JsonProperty("then")
    private List<VariableDefinition> thenVariables = new ArrayList<>();

    @JsonProperty("ifs")
    private List<IfThenBlock> ifThenBlocks = new ArrayList<>();

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

        @JsonProperty("if")
        private List<ConditionDefinition> ifConditions = new ArrayList<>();

        @JsonProperty("then")
        private List<VariableDefinition> thenVariables = new ArrayList<>();
    }
}