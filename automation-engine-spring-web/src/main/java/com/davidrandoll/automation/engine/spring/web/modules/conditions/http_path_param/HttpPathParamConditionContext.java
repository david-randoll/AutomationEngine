package com.davidrandoll.automation.engine.spring.web.modules.conditions.http_path_param;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.davidrandoll.automation.engine.spring.spi.ContextField;
import com.davidrandoll.automation.engine.spring.web.modules.conditions.MatchContext;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.Map;

@Data
@NoArgsConstructor
@FieldNameConstants
@JsonPropertyOrder({
        HttpPathParamConditionContext.Fields.alias,
        HttpPathParamConditionContext.Fields.description,
        HttpPathParamConditionContext.Fields.pathParams
})
public class HttpPathParamConditionContext implements IConditionContext {
    private String alias;
    private String description;

    @ContextField(
        helpText = "Map path parameter names to match conditions (equals, in, regex, like, exists, etc.)"
    )
    @JsonAnySetter
    @JsonAnyGetter
    private Map<String, MatchContext> pathParams;
}