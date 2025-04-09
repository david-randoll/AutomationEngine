package com.automation.engine.creator;

import com.automation.engine.creator.actions.Action;
import com.automation.engine.creator.conditions.Condition;
import com.automation.engine.creator.triggers.Trigger;
import com.automation.engine.creator.variables.Variable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateAutomationRequest {
    private String alias;
    private List<Variable> variables = new ArrayList<>();
    private List<Trigger> triggers = new ArrayList<>();
    private List<Condition> conditions = new ArrayList<>();
    private List<Action> actions = new ArrayList<>();
}