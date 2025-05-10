package com.davidrandoll.automation.engine.creator;

import com.davidrandoll.automation.engine.creator.actions.Action;
import com.davidrandoll.automation.engine.creator.conditions.Condition;
import com.davidrandoll.automation.engine.creator.result.Result;
import com.davidrandoll.automation.engine.creator.triggers.Trigger;
import com.davidrandoll.automation.engine.creator.variables.Variable;
import com.fasterxml.jackson.annotation.JsonAlias;
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

    @JsonAlias({"execution_summary", "result", "summary", "executionResult", "return"})
    private Result result = new Result();
}