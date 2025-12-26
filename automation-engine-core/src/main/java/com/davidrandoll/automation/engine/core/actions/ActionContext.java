package com.davidrandoll.automation.engine.core.actions;

import com.davidrandoll.automation.engine.creator.actions.ActionDefinition;
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

    public ActionContext(ActionDefinition definition) {
        this.alias = definition.getAlias();
        this.description = definition.getDescription();
        this.options = definition.getOptions();
        this.action = definition.getAction();
        this.data = definition.getParams();
    }

    public ActionContext(ActionContext other, Map<String, Object> additionalData) {
        this.alias = other.getAlias();
        this.description = other.getDescription();
        this.options = other.getOptions();
        this.action = other.getAction();
        this.data = additionalData;
    }

    public ActionContext(ActionContext other) {
        this(other, new HashMap<>(other.getData()));
    }

    public ActionContext changeData(Map<String, Object> newData) {
        this.data = newData;
        return this;
    }
}