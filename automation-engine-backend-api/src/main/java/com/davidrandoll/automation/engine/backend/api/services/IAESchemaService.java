package com.davidrandoll.automation.engine.backend.api.services;

import com.davidrandoll.automation.engine.backend.api.dtos.AllBlockWithSchema;
import com.davidrandoll.automation.engine.backend.api.dtos.BlockType;
import com.davidrandoll.automation.engine.backend.api.dtos.BlocksByType;
import com.fasterxml.jackson.databind.JsonNode;

public interface IAESchemaService {
    BlockType getAutomationDefinition();

    BlocksByType getBlocksByType(String moduleType, Boolean includeSchema);

    BlockType getSchemaByBlockName(String name);

    AllBlockWithSchema getAllBlockSchemas();

    /**
     * Generates a comprehensive JSON schema for automation definitions.
     * This schema can be used to validate automation YAML/JSON files and provides
     * IDE autocomplete support. The schema is dynamically built from all registered
     * triggers, conditions, actions, variables, and results.
     *
     * @return A complete JSON schema as a JsonNode
     */
    JsonNode getFullAutomationSchema();
}