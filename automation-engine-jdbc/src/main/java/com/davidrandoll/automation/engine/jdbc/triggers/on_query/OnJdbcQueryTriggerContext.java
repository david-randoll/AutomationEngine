package com.davidrandoll.automation.engine.jdbc.triggers.on_query;

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
    private String alias;
    private String description;

    @JsonAlias({"query", "sql", "statement"})
    private String query;

    @JsonAlias({"params", "parameters", "queryParams", "args", "arguments"})
    private Map<String, Object> params = new HashMap<>();

    private String expression;
}