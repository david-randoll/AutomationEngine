package com.davidrandoll.automation.engine.spring.jdbc.actions.execute;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
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
        JdbcExecuteActionContext.Fields.alias,
        JdbcExecuteActionContext.Fields.description,
        JdbcExecuteActionContext.Fields.query,
        JdbcExecuteActionContext.Fields.params
})
public class JdbcExecuteActionContext implements IActionContext {
    /** Unique identifier for this action */
    private String alias;

    /** Human-readable description of what this action does */
    private String description;

    /** SQL statement to execute (typically INSERT, UPDATE, DELETE, or DDL statements) */
    @JsonAlias({"query", "sql", "statement"})
    private String query;

    /** Parameters to bind to the statement. Map keys correspond to named parameters in the SQL */
    @JsonAlias({"params", "parameters", "queryParams", "args", "arguments"})
    private Map<String, Object> params = new HashMap<>();
}