// API functions for user-defined block types

import { agent, getApiPath } from "./agent";
import type {
  BlockType,
  UserDefinedActionDefinition,
  UserDefinedConditionDefinition,
  UserDefinedTriggerDefinition,
  UserDefinedVariableDefinition,
  UserDefinedDefinition,
} from "@/types/user-defined";
import type { ModuleType } from "@/types/types";

function getBasePath(): string {
  return `${getApiPath()}/user-defined`;
}

// Actions API
export const actionsApi = {
  getAll: () => agent.get<Record<string, UserDefinedActionDefinition>>(`${getBasePath()}/actions`),
  get: (name: string) => agent.get<UserDefinedActionDefinition>(`${getBasePath()}/actions/${name}`),
  register: (definition: UserDefinedActionDefinition) =>
    agent.post<UserDefinedActionDefinition, UserDefinedActionDefinition>(`${getBasePath()}/actions`, definition),
  update: (name: string, definition: UserDefinedActionDefinition) =>
    agent.put<UserDefinedActionDefinition, UserDefinedActionDefinition>(`${getBasePath()}/actions/${name}`, definition),
  unregister: (name: string) => agent.delete(`${getBasePath()}/actions/${name}`),
};

// Conditions API
export const conditionsApi = {
  getAll: () => agent.get<Record<string, UserDefinedConditionDefinition>>(`${getBasePath()}/conditions`),
  get: (name: string) => agent.get<UserDefinedConditionDefinition>(`${getBasePath()}/conditions/${name}`),
  register: (definition: UserDefinedConditionDefinition) =>
    agent.post<UserDefinedConditionDefinition, UserDefinedConditionDefinition>(
      `${getBasePath()}/conditions`,
      definition
    ),
  update: (name: string, definition: UserDefinedConditionDefinition) =>
    agent.put<UserDefinedConditionDefinition, UserDefinedConditionDefinition>(
      `${getBasePath()}/conditions/${name}`,
      definition
    ),
  unregister: (name: string) => agent.delete(`${getBasePath()}/conditions/${name}`),
};

// Triggers API
export const triggersApi = {
  getAll: () => agent.get<Record<string, UserDefinedTriggerDefinition>>(`${getBasePath()}/triggers`),
  get: (name: string) => agent.get<UserDefinedTriggerDefinition>(`${getBasePath()}/triggers/${name}`),
  register: (definition: UserDefinedTriggerDefinition) =>
    agent.post<UserDefinedTriggerDefinition, UserDefinedTriggerDefinition>(`${getBasePath()}/triggers`, definition),
  update: (name: string, definition: UserDefinedTriggerDefinition) =>
    agent.put<UserDefinedTriggerDefinition, UserDefinedTriggerDefinition>(
      `${getBasePath()}/triggers/${name}`,
      definition
    ),
  unregister: (name: string) => agent.delete(`${getBasePath()}/triggers/${name}`),
};

// Variables API
export const variablesApi = {
  getAll: () => agent.get<Record<string, UserDefinedVariableDefinition>>(`${getBasePath()}/variables`),
  get: (name: string) => agent.get<UserDefinedVariableDefinition>(`${getBasePath()}/variables/${name}`),
  register: (definition: UserDefinedVariableDefinition) =>
    agent.post<UserDefinedVariableDefinition, UserDefinedVariableDefinition>(`${getBasePath()}/variables`, definition),
  update: (name: string, definition: UserDefinedVariableDefinition) =>
    agent.put<UserDefinedVariableDefinition, UserDefinedVariableDefinition>(
      `${getBasePath()}/variables/${name}`,
      definition
    ),
  unregister: (name: string) => agent.delete(`${getBasePath()}/variables/${name}`),
};

// Generic API based on block type
export const userDefinedApi = {
  getAll: (type: BlockType) => {
    switch (type) {
      case "actions":
        return actionsApi.getAll();
      case "conditions":
        return conditionsApi.getAll();
      case "triggers":
        return triggersApi.getAll();
      case "variables":
        return variablesApi.getAll();
    }
  },
  get: (type: BlockType, name: string) => {
    switch (type) {
      case "actions":
        return actionsApi.get(name);
      case "conditions":
        return conditionsApi.get(name);
      case "triggers":
        return triggersApi.get(name);
      case "variables":
        return variablesApi.get(name);
    }
  },
  register: (type: BlockType, definition: UserDefinedDefinition) => {
    switch (type) {
      case "actions":
        return actionsApi.register(definition as UserDefinedActionDefinition);
      case "conditions":
        return conditionsApi.register(definition as UserDefinedConditionDefinition);
      case "triggers":
        return triggersApi.register(definition as UserDefinedTriggerDefinition);
      case "variables":
        return variablesApi.register(definition as UserDefinedVariableDefinition);
    }
  },
  update: (type: BlockType, name: string, definition: UserDefinedDefinition) => {
    switch (type) {
      case "actions":
        return actionsApi.update(name, definition as UserDefinedActionDefinition);
      case "conditions":
        return conditionsApi.update(name, definition as UserDefinedConditionDefinition);
      case "triggers":
        return triggersApi.update(name, definition as UserDefinedTriggerDefinition);
      case "variables":
        return variablesApi.update(name, definition as UserDefinedVariableDefinition);
    }
  },
  unregister: (type: BlockType, name: string) => {
    switch (type) {
      case "actions":
        return actionsApi.unregister(name);
      case "conditions":
        return conditionsApi.unregister(name);
      case "triggers":
        return triggersApi.unregister(name);
      case "variables":
        return variablesApi.unregister(name);
    }
  },
  getSchema: (type: BlockType) => agent.get<ModuleType>(`${getBasePath()}/${type}/schema`),
};
