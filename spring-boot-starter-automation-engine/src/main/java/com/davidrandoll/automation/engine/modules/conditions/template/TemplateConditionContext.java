package com.davidrandoll.automation.engine.modules.conditions.template;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplateConditionContext implements IConditionContext {
    private String alias;
    private String description;
    private String expression;
}