package com.davidrandoll.automation.engine.modules.variable.basic;

import com.davidrandoll.automation.engine.core.variables.IVariableContext;
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
public class BasicVariableContext implements IVariableContext {
    private String alias;

    @JsonAnySetter
    @JsonAnyGetter
    private Map<String, Object> variables = new HashMap<>();
}
