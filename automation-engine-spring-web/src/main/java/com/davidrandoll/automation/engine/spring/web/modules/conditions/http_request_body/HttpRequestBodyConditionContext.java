package com.davidrandoll.automation.engine.spring.web.modules.conditions.http_request_body;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HttpRequestBodyConditionContext implements IConditionContext {
    private String alias;
    private String description;
    private JsonNode requestBody;
}