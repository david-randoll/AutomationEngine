package com.davidrandoll.automation.engine.spring.web.modules.conditions.http_header;

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
        HttpHeaderConditionContext.Fields.alias,
        HttpHeaderConditionContext.Fields.description
})
public class HttpHeaderConditionContext implements IConditionContext {
    /** Unique identifier for this condition */
    private String alias;

    /** Human-readable description of what this condition checks */
    private String description;

    /** Map of HTTP header names to match conditions. Each header can have match operations (equals, in, regex, etc.) */
    @ContextField(
        helpText = "Map header names to match conditions (equals, in, regex, like, exists, etc.)"
    )
    @JsonAnySetter
    @JsonAnyGetter
    private Map<String, MatchContext> headers;
}