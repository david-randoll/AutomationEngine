package com.automation.engine.actions;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ActionContext {
    private Map<String, Object> data;
}
