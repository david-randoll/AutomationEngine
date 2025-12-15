package com.davidrandoll.automation.engine.spring.jdbc.triggers.on_query;

import com.davidrandoll.automation.engine.core.triggers.ITriggerContext;
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
        OnJdbcQueryTriggerContext.Fields.alias,
        OnJdbcQueryTriggerContext.Fields.description,
        OnJdbcQueryTriggerContext.Fields.query,
        OnJdbcQueryTriggerContext.Fields.params,
        OnJdbcQueryTriggerContext.Fields.expression
})
public class OnJdbcQueryTriggerContext implements ITriggerContext {
    /** Unique identifier for this trigger */
    private String alias;

    /** Human-readable description of what this trigger responds to */
    private String description;

    /** SQL query to execute for evaluation. Can contain placeholders for parameters */
    @JsonAlias({"query", "sql", "statement"})
    private String query;

    /** Parameters to bind to the query. Map keys correspond to named parameters in the SQL query */
    @JsonAlias({"params", "parameters", "queryParams", "args", "arguments"})
    private Map<String, Object> params = new HashMap<>();

    /** Template expression to evaluate against the query result. Trigger activates if expression evaluates to true */
    private String expression;
}