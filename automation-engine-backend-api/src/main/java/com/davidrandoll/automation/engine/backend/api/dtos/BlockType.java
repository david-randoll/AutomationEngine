package com.davidrandoll.automation.engine.backend.api.dtos;

import com.davidrandoll.automation.engine.core.IBlock;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockType {
    private String name;
    private String label;
    private String description;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private JsonNode schema;

    public BlockType(String name, IBlock module, @Nullable JsonNode schema) {
        this.name = name;
        this.label = module.getModuleLabel();
        this.description = module.getModuleDescription();
        this.schema = schema;
    }
}
