package com.davidrandoll.automation.engine.spring.modules.conditions.template;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.davidrandoll.automation.engine.spring.spi.ContextField;
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

    @ContextField(
        widget = ContextField.Widget.TEXTAREA,
        placeholder = "{{ event.amount > 100 }}",
        helpText = "Pebble template expression that evaluates to true/false. Access event data via {{ event.fieldName }}"
    )
    private String expression;
}