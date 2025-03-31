package com.automation.engine.modules.conditions.template;

import com.automation.engine.core.conditions.IConditionContext;
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
    private String expression;
}