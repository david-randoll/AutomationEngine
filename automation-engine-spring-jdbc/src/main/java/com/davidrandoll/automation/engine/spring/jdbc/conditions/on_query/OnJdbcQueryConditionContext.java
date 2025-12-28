package com.davidrandoll.automation.engine.spring.jdbc.conditions.on_query;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.davidrandoll.automation.engine.spring.spi.ContextField;
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

    @ContextField(
        widget = ContextField.Widget.MONACO_EDITOR,
        monacoLanguage = "sql",
        helpText = "SQL query whose result will be evaluated by the expression"
    )
    @JsonAlias({"query", "sql", "statement"})
    private String query;

    @ContextField(
        helpText = "Key-value pairs for query parameters. Keys must match :paramName placeholders"
    )
    @JsonAlias({"params", "parameters", "queryParams", "args", "arguments"})
    private Map<String, Object> params = new HashMap<>();

    @ContextField(
        widget = ContextField.Widget.TEXTAREA,
        placeholder = "{{ result | length > 0 }}",
        helpText = "Pebble expression evaluated against query result. Use {{ result }} to access rows"
    )
    private String expression;
}