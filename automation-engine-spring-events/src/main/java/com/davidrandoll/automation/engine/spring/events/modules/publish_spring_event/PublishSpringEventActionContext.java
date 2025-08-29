package com.davidrandoll.automation.engine.spring.events.modules.publish_spring_event;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldNameConstants
@JsonPropertyOrder({
        PublishSpringEventActionContext.Fields.alias,
        PublishSpringEventActionContext.Fields.description,
        PublishSpringEventActionContext.Fields.className,
        PublishSpringEventActionContext.Fields.publishToAutomationEngine,
        PublishSpringEventActionContext.Fields.data
})
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
