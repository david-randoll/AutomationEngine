package com.davidrandoll.automation.engine.spring.web.modules.conditions.http_header;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
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
    private String alias;
    private String description;

    @JsonAnySetter
    @JsonAnyGetter
    private Map<String, MatchContext> headers;
}