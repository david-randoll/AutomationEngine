package com.davidrandoll.automation.engine.spring.jdbc.conditions.on_query;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@FieldNameConstants
@JsonPropertyOrder({
        OnJdbcQueryConditionContext.Fields.alias,
        OnJdbcQueryConditionContext.Fields.description,
        OnJdbcQueryConditionContext.Fields.query,
        OnJdbcQueryConditionContext.Fields.params,
        OnJdbcQueryConditionContext.Fields.expression
})
public class OnJdbcQueryConditionContext implements IConditionContext {
    private String alias;
    private String description;

    @JsonAlias({"query", "sql", "statement"})
    private String query;

    @JsonAlias({"params", "parameters", "queryParams", "args", "arguments"})
    private Map<String, Object> params = new HashMap<>();

    private String expression;
}