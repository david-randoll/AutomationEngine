package com.davidrandoll.automation.engine.core.triggers;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class TriggerContext {
    private Map<String, Object> data;
}
