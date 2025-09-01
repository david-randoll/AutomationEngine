package com.davidrandoll.automation.engine.backend.api.services;

import com.davidrandoll.automation.engine.backend.api.dtos.AllBlockWithSchema;
import com.davidrandoll.automation.engine.backend.api.dtos.BlockType;
import com.davidrandoll.automation.engine.backend.api.dtos.BlocksByType;

public interface IAESchemaService {
    BlockType getAutomationDefinition();
    BlocksByType getBlocksByType(String moduleType, Boolean includeSchema);
    BlockType getSchemaByBlockName(String name);
    AllBlockWithSchema getAllBlockSchemas();
}