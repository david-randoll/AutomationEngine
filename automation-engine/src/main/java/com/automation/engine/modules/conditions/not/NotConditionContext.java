package com.automation.engine.modules.conditions.not;

import com.automation.engine.core.conditions.IConditionContext;
import com.automation.engine.factory.model.Condition;
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
public class NotConditionContext implements IConditionContext {
    private String alias;
    private List<Condition> conditions = new ArrayList<>();
}