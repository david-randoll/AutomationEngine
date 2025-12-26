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
public class TriggerContext implements ITriggerContext {
    private String alias;
    private String description;
    private Map<String, Object> options = new HashMap<>();

    @NotEmpty
    @JsonAlias({"type", "trigger"})
    @JsonIgnore
    private String trigger;

    @JsonIgnore
    @JsonAnyGetter
    @JsonAnySetter
    @JsonProperty("0829b1b94f764e47b871865ea6628f34")
    private Map<String, Object> data;

    public TriggerContext(String alias, String description, String type, Map<String, Object> data) {
        this.alias = alias;
        this.description = description;
        this.trigger = type;
        this.data = data != null ? data : new HashMap<>();
    }

    public TriggerContext(String alias, String description, String action, Map<String, Object> data, Map<String, Object> options) {
        this(alias, description, action, data);
        this.options = options;
    }

    public TriggerContext(TriggerDefinition definition) {
        this(definition.getAlias(), definition.getDescription(), definition.getTrigger(), definition.getParams(), definition.getOptions());
    }

    public TriggerContext(TriggerContext other, Map<String, Object> additionalData) {
        this(other.getAlias(), other.getDescription(), other.getTrigger(), additionalData, other.getOptions());
    }

    public TriggerContext(TriggerContext other) {
        this(other, new HashMap<>(other.getData()));
    }

    public TriggerContext changeData(Map<String, Object> mapCopy) {
        this.data = mapCopy;
        return this;
    }
}