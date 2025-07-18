package com.davidrandoll.automation.engine.modules.actions.if_then_else;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.creator.actions.ActionDefinition;
import com.davidrandoll.automation.engine.creator.conditions.ConditionDefinition;
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
public class IfThenElseActionContext implements IActionContext {
    private String alias;
    private String description;

    @JsonProperty("if")
    private List<ConditionDefinition> ifConditions = new ArrayList<>();

    @JsonProperty("then")
    private List<ActionDefinition> thenActions = new ArrayList<>();

    @JsonProperty("ifs")
    private List<IfThenBlock> ifThenBlocks = new ArrayList<>();

    @JsonProperty("else")
    private List<ActionDefinition> elseActions = new ArrayList<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IfThenBlock {
        private String alias;

        @JsonProperty("if")
        private List<ConditionDefinition> ifConditions = new ArrayList<>();

        @JsonProperty("then")
        private List<ActionDefinition> thenActions = new ArrayList<>();
    }
}