package com.automation.engine.modules.actions.sequence;

import com.automation.engine.core.actions.IActionContext;
import com.automation.engine.factory.model.Action;
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
    private List<Action> actions;
}