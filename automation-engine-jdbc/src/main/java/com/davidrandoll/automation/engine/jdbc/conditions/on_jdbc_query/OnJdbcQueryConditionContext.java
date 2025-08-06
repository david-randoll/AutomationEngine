package com.davidrandoll.automation.engine.jdbc.conditions.on_jdbc_query;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class OnJdbcQueryConditionContext implements IConditionContext {
    private String alias;
    private String description;

    @JsonAlias({"query", "sql", "statement"})
    private String query;

    @JsonAlias({"params", "parameters", "queryParams", "args", "arguments"})
    private Map<String, Object> params = new HashMap<>();

    private String expression;
}