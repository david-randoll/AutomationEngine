package com.davidrandoll.automation.engine.spring.web.modules.conditions.http_request_body;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@NoArgsConstructor
@FieldNameConstants
@JsonPropertyOrder({
        HttpRequestBodyConditionContext.Fields.alias,
        HttpRequestBodyConditionContext.Fields.description,
        HttpRequestBodyConditionContext.Fields.requestBody
})
public class HttpRequestBodyConditionContext implements IConditionContext {
    private String alias;
    private String description;
    private JsonNode requestBody;
}