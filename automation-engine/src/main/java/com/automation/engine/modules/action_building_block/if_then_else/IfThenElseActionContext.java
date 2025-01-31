package com.automation.engine.modules.action_building_block.if_then_else;

import com.automation.engine.factory.request.Action;
import com.automation.engine.factory.request.Condition;
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
public class IfThenElseActionContext {
    @JsonProperty("if")
    private List<Condition> ifConditions = new ArrayList<>();

    @JsonProperty("then")
    private List<Action> thenActions = new ArrayList<>();

    @JsonProperty("else")
    private List<Action> elseActions = new ArrayList<>();
}