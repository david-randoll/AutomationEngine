package com.davidrandoll.automation.engine.spring.events.modules.publish_spring_event;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublishSpringEventActionContext implements IActionContext {
    private String alias;
    private String description;

    private String className;

    @JsonAlias({"publishToAE", "publishToAutomationEngine", "ae"})
    private boolean publishToAutomationEngine = false;

    @JsonAnySetter
    @JsonAnyGetter
    private Map<String, Object> data;
}
