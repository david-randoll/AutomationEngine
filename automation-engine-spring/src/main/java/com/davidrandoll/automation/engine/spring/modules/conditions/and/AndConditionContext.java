package com.davidrandoll.automation.engine.spring.modules.conditions.and;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.davidrandoll.automation.engine.creator.conditions.ConditionDefinition;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
        AndConditionContext.Fields.alias,
        AndConditionContext.Fields.description,
        AndConditionContext.Fields.conditions
})
public class AndConditionContext implements IConditionContext {
    private String alias;
    private String description;
    private List<ConditionDefinition> conditions = new ArrayList<>();
}