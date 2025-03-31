package com.automation.engine.modules.actions.variable;

import com.automation.engine.core.actions.IActionContext;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariableActionContext implements IActionContext {
    private String alias;

    @JsonAnySetter
    @JsonAnyGetter
    private Map<String, Object> variables = new HashMap<>();
}
