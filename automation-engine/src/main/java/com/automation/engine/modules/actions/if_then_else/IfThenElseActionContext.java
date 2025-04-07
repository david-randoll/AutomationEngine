package com.automation.engine.modules.actions.if_then_else;

import com.automation.engine.core.actions.IActionContext;
import com.automation.engine.factory.actions.Action;
import com.automation.engine.factory.conditions.Condition;
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

    @JsonProperty("if")
    private List<Condition> ifConditions = new ArrayList<>();

    @JsonProperty("then")
    private List<Action> thenActions = new ArrayList<>();

    @JsonProperty("ifs")
    private List<IfThenBlock> ifThenBlocks = new ArrayList<>();

    @JsonProperty("else")
    private List<Action> elseActions = new ArrayList<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IfThenBlock {
        private String alias;

        @JsonProperty("if")
        private List<Condition> ifConditions = new ArrayList<>();

        @JsonProperty("then")
        private List<Action> thenActions = new ArrayList<>();
    }
}