package com.davidrandoll.automation.engine.modules.triggers.template;

import com.davidrandoll.automation.engine.core.triggers.ITriggerContext;
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
        TemplateTriggerContext.Fields.alias,
        TemplateTriggerContext.Fields.description,
        TemplateTriggerContext.Fields.expression
})
public class TemplateTriggerContext implements ITriggerContext {
    private String alias;
    private String description;
    private String expression;
}