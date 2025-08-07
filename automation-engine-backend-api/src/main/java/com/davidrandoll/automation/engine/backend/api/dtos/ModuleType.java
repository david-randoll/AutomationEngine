package com.davidrandoll.automation.engine.backend.api.dtos;

import com.davidrandoll.automation.engine.core.IModule;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Data
@NoArgsConstructor
public class ModuleType {
    private String name;
    private String label;
    private String description;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private JsonNode schema;

    public ModuleType(String name, IModule module, @Nullable JsonNode schema) {
        this.name = name;
        this.label = module.getModuleLabel();
        this.description = module.getModuleDescription();
        this.schema = schema;
    }
}
