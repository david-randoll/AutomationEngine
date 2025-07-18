package com.davidrandoll.automation.engine.creator.result;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class ResultDeserializer extends JsonDeserializer<ResultDefinition> {

    @Override
    public ResultDefinition deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        if (node.isObject() && !node.isEmpty()) {
            // let jackson handle the deserialization
            return p.getCodec().treeToValue(node, ResultDefinition.class);
        } else {
            // can be primitive, text or array
            ResultDefinition result = new ResultDefinition();
            result.setParams(node);
            return result;
        }
    }
}