package com.davidrandoll.automation.engine.core.result;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ResultContext {
    private Map<String, Object> data;
}