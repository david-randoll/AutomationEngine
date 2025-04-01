package com.automation.engine.modules.triggers.template;

import com.automation.engine.core.triggers.ITriggerContext;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplateTriggerContext implements ITriggerContext {
    private String alias;
    private String expression;
}