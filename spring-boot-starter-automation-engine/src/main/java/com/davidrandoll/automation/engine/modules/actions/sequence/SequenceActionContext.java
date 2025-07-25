package com.davidrandoll.automation.engine.modules.actions.sequence;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.creator.actions.ActionDefinition;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SequenceActionContext implements IActionContext {
    private String alias;
    private String description;
    private List<ActionDefinition> actions;
}