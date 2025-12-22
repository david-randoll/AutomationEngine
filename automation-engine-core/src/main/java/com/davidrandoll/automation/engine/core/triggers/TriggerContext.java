package com.davidrandoll.automation.engine.core.triggers;

import com.davidrandoll.automation.engine.creator.triggers.TriggerDefinition;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TriggerContext {
    private String alias;
    private String description;

    @NotEmpty
    @JsonAlias({"type", "trigger"})
    @JsonIgnore
    private String trigger;

    @JsonIgnore
    @JsonAnyGetter
    @JsonAnySetter
    @JsonProperty("0829b1b94f764e47b871865ea6628f34")
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

    public TriggerContext(TriggerContext other) {
        this(other, new HashMap<>(other.getData()));
    }

    public TriggerContext changeData(Map<String, Object> mapCopy) {
        this.data = mapCopy;
        return this;
    }
}