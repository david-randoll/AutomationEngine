package com.davidrandoll.automation.engine.spring.web.modules.conditions.http_query_param;

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
        HttpQueryParamConditionContext.Fields.alias,
        HttpQueryParamConditionContext.Fields.description,
        HttpQueryParamConditionContext.Fields.queryParams
})
public class HttpQueryParamConditionContext implements IConditionContext {
    private String alias;
    private String description;

    @JsonAnySetter
    @JsonAnyGetter
    private Map<String, MatchContext> queryParams;
}