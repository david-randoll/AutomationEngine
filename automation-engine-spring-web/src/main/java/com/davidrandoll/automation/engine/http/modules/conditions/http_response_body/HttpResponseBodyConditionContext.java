package com.davidrandoll.automation.engine.http.modules.conditions.http_response_body;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HttpResponseBodyConditionContext implements IConditionContext {
    private String alias;
    private String description;
    private JsonNode responseBody;
}