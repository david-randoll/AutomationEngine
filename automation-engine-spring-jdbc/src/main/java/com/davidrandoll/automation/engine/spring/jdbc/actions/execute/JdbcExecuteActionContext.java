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
    private String alias;
    private String description;

    @JsonAlias({"query", "sql", "statement"})
    private String query;

    @JsonAlias({"params", "parameters", "queryParams", "args", "arguments"})
    private Map<String, Object> params = new HashMap<>();
}