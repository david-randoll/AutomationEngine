package com.davidrandoll.automation.engine.creator;

import com.davidrandoll.automation.engine.creator.actions.Action;
import com.davidrandoll.automation.engine.creator.conditions.Condition;
import com.davidrandoll.automation.engine.creator.result.Result;
import com.davidrandoll.automation.engine.creator.result.ResultDeserializer;
import com.davidrandoll.automation.engine.creator.triggers.Trigger;
import com.davidrandoll.automation.engine.creator.variables.Variable;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateAutomationRequest {
    @Getter
    private String alias;
    private List<Variable> variables = new ArrayList<>();
    private List<Trigger> triggers = new ArrayList<>();
    private List<Condition> conditions = new ArrayList<>();
    private List<Action> actions = new ArrayList<>();

    @JsonAlias({"execution_summary", "result", "summary", "executionResult", "return"})
    @JsonDeserialize(using = ResultDeserializer.class)
    private Result result = new Result();

    public List<Variable> getVariables() {
        return getNonNullList(variables);
    }

    public List<Trigger> getTriggers() {
        return getNonNullList(triggers);
    }

    public List<Condition> getConditions() {
        return getNonNullList(conditions);
    }

    public List<Action> getActions() {
        return getNonNullList(actions);
    }

    public Result getResult() {
        return Optional.ofNullable(result).orElse(new Result());
    }

    private <T> List<T> getNonNullList(List<T> list) {
        return Optional.ofNullable(list)
                .orElse(new ArrayList<>())
                .stream()
                .filter(Objects::nonNull)
                .toList();
    }
}