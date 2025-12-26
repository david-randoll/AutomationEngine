package com.davidrandoll.automation.engine.core.result;

import com.davidrandoll.automation.engine.creator.result.ResultDefinition;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
public class ResultContext implements IResultContext {
    private String alias;
    private String description;
    private Map<String, Object> options = new HashMap<>();

    @JsonAlias({"result", "return", "type"})
    @JsonIgnore
    private String result = "basic";

    @JsonIgnore
    @JsonAnySetter
    @JsonProperty("0829b1b94f764e47b871865ea6628f34")
    private JsonNode data;

    public ResultContext(String alias, String description, String type, JsonNode data) {
        this.alias = alias;
        this.description = description;
        this.result = type;
        this.data = data;
    }

    public ResultContext(String alias, String description, String result, JsonNode data, Map<String, Object> options) {
        this(alias, description, result, data);
        this.options = options;
    }

    public ResultContext(ResultDefinition definition) {
        this(definition.getAlias(), definition.getDescription(), definition.getResult(), definition.getParams(), definition.getOptions());
    }

    public ResultContext(ResultContext other, JsonNode additionalData) {
        this(other.getAlias(), other.getDescription(), other.getResult(), additionalData, other.getOptions());
    }

    public ResultContext(ResultContext other) {
        this(other, other.getData() != null ? other.getData().deepCopy() : null);
    }

    public ResultContext changeData(JsonNode newData) {
        this.data = newData;
        return this;
    }

    public JsonNode getResultData() {
        // add the alias, description, and result type into the data node
        // if data is an object node, add fields, else return data as is
        if (data != null && data.isObject()) {
            ((ObjectNode) data).put(Fields.alias, alias);
            ((ObjectNode) data).put(Fields.description, description);
            ((ObjectNode) data).put(Fields.result, result);
        }
        return data;
    }
}