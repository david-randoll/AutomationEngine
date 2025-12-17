// API functions for automation engine blocks and schemas

import { agent, getApiPath } from "./agent";
import type { ModuleType, Area } from "@/types/types";

function getBasePath(): string {
  return getApiPath();
}

// Response type for block list endpoint
export interface BlockListResponse {
  types: ModuleType[];
}

// Block API - for fetching available blocks and their schemas
export const blockApi = {
  /**
   * Get all blocks of a specific area type (variable, trigger, condition, action, result)
   * @param area - The area type to fetch blocks for
   * @param includeSchema - Whether to include the schema in the response
   */
  getAll: (area: Area, includeSchema = true) =>
    agent.get<BlockListResponse>(`${getBasePath()}/block/${area}?includeSchema=${includeSchema}`),

  /**
   * Get all blocks of a specific area type, returning raw API response
   * @param area - The area type to fetch blocks for
   * @param includeSchema - Whether to include the schema in the response
   */
  getAllHttp: (area: Area, includeSchema = true) =>
    agent.getHttp<BlockListResponse>(`${getBasePath()}/block/${area}?includeSchema=${includeSchema}`),

  /**
   * Get the schema for a specific block by name
   * @param blockName - The name of the block
   */
  getSchema: (blockName: string) => agent.get<ModuleType>(`${getBasePath()}/block/${blockName}/schema`),

  /**
   * Get the schema for a specific block by name, returning raw API response
   * @param blockName - The name of the block
   */
  getSchemaHttp: (blockName: string) => agent.getHttp<ModuleType>(`${getBasePath()}/block/${blockName}/schema`),
};

// Automation Definition API - for automation definition schema
export const automationDefinitionApi = {
  /**
   * Get the schema for automation definitions
   */
  getSchema: () => agent.get<ModuleType>(`${getBasePath()}/automation-definition/schema`),

  /**
   * Get the schema for automation definitions, returning raw API response
   */
  getSchemaHttp: () => agent.getHttp<ModuleType>(`${getBasePath()}/automation-definition/schema`),
};

// Combined automation API export
export const automationApi = {
  block: blockApi,
  automationDefinition: automationDefinitionApi,
};
