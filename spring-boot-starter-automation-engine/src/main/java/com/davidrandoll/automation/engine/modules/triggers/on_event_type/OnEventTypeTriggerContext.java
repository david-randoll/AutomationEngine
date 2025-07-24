package com.davidrandoll.automation.engine.modules.triggers.on_event_type;

import com.davidrandoll.automation.engine.core.triggers.ITriggerContext;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OnEventTypeTriggerContext implements ITriggerContext {
    private String alias;
    private String description;
    private String eventType;
    private String eventName;
    private String regex;
}