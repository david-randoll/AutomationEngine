package com.davidrandoll.automation.engine.core.actions;

import com.davidrandoll.automation.engine.creator.actions.ActionDefinition;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class ActionContext implements IActionContext {
    private String alias;
    private String description;
    private Map<String, Object> options = new HashMap<>();

    @NotEmpty
    @JsonAlias({"action", "type"})
    @JsonIgnore
    private String action;

    @JsonIgnore
    @JsonAnyGetter
    @JsonAnySetter
    @JsonProperty("0829b1b94f764e47b871865ea6628f34")
    private Map<String, Object> data;

    public ActionContext(String alias, String description, String type, Map<String, Object> data) {
        this.alias = alias;
        this.description = description;
        this.action = type;
        this.data = data != null ? data : new HashMap<>();
    }

    public ActionContext(String alias, String description, String action, Map<String, Object> data, Map<String, Object> options) {
        this(alias, description, action, data);
        this.options = options;
    }

    public ActionContext(ActionDefinition definition) {
        this(definition.getAlias(), definition.getDescription(), definition.getAction(), definition.getParams(), definition.getOptions());
    }

    public ActionContext(ActionContext other, Map<String, Object> additionalData) {
        this(other.getAlias(), other.getDescription(), other.getAction(), additionalData, other.getOptions());
    }

    public ActionContext(ActionContext other) {
        this(other, new HashMap<>(other.getData()));
    }

    public ActionContext changeData(Map<String, Object> newData) {
        this.data = newData;
        return this;
    }
}