package com.davidrandoll.automation.engine.jdbc.actions.query;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class JdbcQueryActionContext implements IActionContext {
    private String alias;
    private String description;

    @JsonAlias({"query", "sql", "statement"})
    private String query;

    @JsonAlias({"params", "parameters", "queryParams", "args", "arguments"})
    private Map<String, Object> params = new HashMap<>();

    /**
     * Mode of the query: "list" or "single"
     * - "list" returns List<Map<String, Object>>
     * - "single" returns a Map<String, Object> or null
     */
    private String mode = "list";

    /**
     * Variable name where to store the result
     */
    @JsonAlias({"resultVariable", "resultVar", "outputVariable", "outputVar", "storeAs", "storeToVariable"})
    private String variable = "queryResult";
}
