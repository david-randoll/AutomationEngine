package com.davidrandoll.automation.engine.spring.modules.conditions.template;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldNameConstants
@JsonPropertyOrder({
        TemplateConditionContext.Fields.alias,
        TemplateConditionContext.Fields.description,
        TemplateConditionContext.Fields.expression
})
public class TemplateConditionContext implements IConditionContext {
    private String alias;
    private String description;
    private String expression;
}