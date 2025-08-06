package com.davidrandoll.automation.engine.modules.variables.if_them_else;

import com.davidrandoll.automation.engine.core.variables.IVariableContext;
import com.davidrandoll.automation.engine.creator.conditions.ConditionDefinition;
import com.davidrandoll.automation.engine.creator.variables.VariableDefinition;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
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
    public static class IfThenBlock implements IVariableContext {
        private String alias;
        private String description;

        @JsonProperty("if")
        private List<ConditionDefinition> ifConditions = new ArrayList<>();

        @JsonProperty("then")
        private List<VariableDefinition> thenVariables = new ArrayList<>();
    }
}