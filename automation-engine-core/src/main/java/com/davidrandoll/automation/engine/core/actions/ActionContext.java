package com.davidrandoll.automation.engine.core.actions;

import com.davidrandoll.automation.engine.creator.actions.ActionDefinition;
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
public class ActionContext {
    private String alias;
    private String description;

    @NotEmpty
    @JsonAlias({"action", "type"})
    private String action;

    @JsonIgnore
    @JsonAnyGetter
    @JsonAnySetter
    private Map<String, Object> data;

    public ActionContext(ActionDefinition definition) {
        this.alias = definition.getAlias();
        this.description = definition.getDescription();
        this.action = definition.getAction();
        this.data = definition.getParams();
    }

    public ActionContext(ActionContext other, Map<String, Object> additionalData) {
        this.alias = other.getAlias();
        this.description = other.getDescription();
        this.action = other.getAction();
        this.data = additionalData;
    }
}