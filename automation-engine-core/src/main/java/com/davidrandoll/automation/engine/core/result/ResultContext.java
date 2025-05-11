package com.davidrandoll.automation.engine.core.result;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResultContext {
    private JsonNode data;
}