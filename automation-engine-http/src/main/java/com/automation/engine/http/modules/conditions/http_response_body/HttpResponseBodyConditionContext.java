package com.automation.engine.http.modules.conditions.http_response_body;

import com.automation.engine.core.conditions.IConditionContext;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HttpResponseBodyConditionContext implements IConditionContext {
    private String alias;
    private JsonNode responseBody;
}