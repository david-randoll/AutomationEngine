package com.davidrandoll.automation.engine.modules.conditions.on_event_type;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OnEventTypeConditionContext implements IConditionContext {
    private String alias;
    private String description;
    private String eventType;
    private String eventName;
    private String regex;
}