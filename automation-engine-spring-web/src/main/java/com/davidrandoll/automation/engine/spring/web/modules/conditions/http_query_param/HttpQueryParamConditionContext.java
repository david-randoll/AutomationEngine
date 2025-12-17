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
    /** Unique identifier for this condition */
    private String alias;

    /** Human-readable description of what this condition checks */
    private String description;

    /** Map of query parameter names to match conditions. Each parameter can have match operations (equals, in, regex, etc.) */
    @JsonAnySetter
    @JsonAnyGetter
    private Map<String, MatchContext> queryParams;
}