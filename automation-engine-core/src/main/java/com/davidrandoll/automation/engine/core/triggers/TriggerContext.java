package com.davidrandoll.automation.engine.core.triggers;

import com.davidrandoll.automation.engine.creator.triggers.TriggerDefinition;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TriggerContext {
    private String alias;
    private String description;

    @NotEmpty
    @JsonAlias({"type", "trigger"})
    private String trigger;

    @JsonIgnore
    @JsonAnyGetter
    @JsonAnySetter
    private Map<String, Object> data;

    public TriggerContext(TriggerDefinition definition) {
        this.alias = definition.getAlias();
        this.description = definition.getDescription();
        this.trigger = definition.getTrigger();
        this.data = definition.getParams();
    }

    public TriggerContext(TriggerContext other, Map<String, Object> additionalData) {
        this.alias = other.getAlias();
        this.description = other.getDescription();
        this.trigger = other.getTrigger();
        this.data = additionalData;
    }
}
