package com.davidrandoll.automation.engine.modules.conditions.and;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.davidrandoll.automation.engine.creator.conditions.ConditionDefinition;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AndConditionContext implements IConditionContext {
    private String alias;
    private String description;
    private List<ConditionDefinition> conditions = new ArrayList<>();
}