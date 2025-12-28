package com.davidrandoll.automation.engine.spring.jdbc.actions.query;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
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
        JdbcQueryActionContext.Fields.alias,
        JdbcQueryActionContext.Fields.description,
        JdbcQueryActionContext.Fields.query,
        JdbcQueryActionContext.Fields.params,
        JdbcQueryActionContext.Fields.mode,
        JdbcQueryActionContext.Fields.variable
})
public class JdbcQueryActionContext implements IActionContext {
    /**
     * Unique identifier for this action
     */
    private String alias;

    /**
     * Human-readable description of what this action does
     */
    private String description;

    /**
     * SQL query to execute. Can contain placeholders for parameters (e.g., :paramName or ?)
     */
    @ContextField(
        widget = ContextField.Widget.MONACO_EDITOR,
        monacoLanguage = "sql",
        helpText = "SQL SELECT query. Use :paramName for named parameters (e.g., SELECT * FROM users WHERE id = :userId)"
    )
    @JsonAlias({"query", "sql", "statement"})
    private String query;

    /**
     * Parameters to bind to the query. Map keys correspond to named parameters in the SQL query
     */
    @ContextField(
        helpText = "Key-value pairs for query parameters. Keys must match :paramName placeholders in SQL"
    )
    @JsonAlias({"params", "parameters", "queryParams", "args", "arguments"})
    private Map<String, Object> params = new HashMap<>();

    /**
     * Mode of the query: "list" or "single"
     * - "list" returns List<Map<String, Object>>
     * - "single" returns a Map<String, Object> or null
     */
    @ContextField(
        widget = ContextField.Widget.DROPDOWN,
        dropdownOptions = {"list", "single"},
        dropdownLabels = {"List (multiple rows)", "Single (one row or null)"},
        helpText = "Result mode: 'list' for multiple rows, 'single' for one row"
    )
    private String mode = "list";

    /**
     * Variable name where to store the result
     */
    @ContextField(
        placeholder = "queryResult",
        helpText = "Variable name to store results. Access via {{ queryResult }} or {{ queryResult[0].column }}"
    )
    @JsonAlias({"resultVariable", "resultVar", "outputVariable", "outputVar", "storeAs", "storeToVariable"})
    private String variable = "queryResult";
}
