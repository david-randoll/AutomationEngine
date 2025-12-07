// API functions for user-defined block types

import { agent } from "./agent";
import type {
  BlockType,
  UserDefinedActionDefinition,
  UserDefinedConditionDefinition,
  UserDefinedTriggerDefinition,
  UserDefinedVariableDefinition,
  UserDefinedDefinition,
} from "@/types/user-defined";
import type { ModuleType } from "@/types/types";

const BASE_PATH = "/automation-engine/user-defined";

// Actions API
export const actionsApi = {
  getAll: () => agent.get<Record<string, UserDefinedActionDefinition>>(`${BASE_PATH}/actions`),
  get: (name: string) => agent.get<UserDefinedActionDefinition>(`${BASE_PATH}/actions/${name}`),
  register: (definition: UserDefinedActionDefinition) =>
    agent.post<UserDefinedActionDefinition, UserDefinedActionDefinition>(`${BASE_PATH}/actions`, definition),
  update: (name: string, definition: UserDefinedActionDefinition) =>
    agent.put<UserDefinedActionDefinition, UserDefinedActionDefinition>(`${BASE_PATH}/actions/${name}`, definition),
  unregister: (name: string) => agent.delete(`${BASE_PATH}/actions/${name}`),
};

// Conditions API
export const conditionsApi = {
  getAll: () => agent.get<Record<string, UserDefinedConditionDefinition>>(`${BASE_PATH}/conditions`),
  get: (name: string) => agent.get<UserDefinedConditionDefinition>(`${BASE_PATH}/conditions/${name}`),
  register: (definition: UserDefinedConditionDefinition) =>
    agent.post<UserDefinedConditionDefinition, UserDefinedConditionDefinition>(`${BASE_PATH}/conditions`, definition),
  update: (name: string, definition: UserDefinedConditionDefinition) =>
    agent.put<UserDefinedConditionDefinition, UserDefinedConditionDefinition>(
      `${BASE_PATH}/conditions/${name}`,
      definition
    ),
  unregister: (name: string) => agent.delete(`${BASE_PATH}/conditions/${name}`),
};

// Triggers API
export const triggersApi = {
  getAll: () => agent.get<Record<string, UserDefinedTriggerDefinition>>(`${BASE_PATH}/triggers`),
  get: (name: string) => agent.get<UserDefinedTriggerDefinition>(`${BASE_PATH}/triggers/${name}`),
  register: (definition: UserDefinedTriggerDefinition) =>
    agent.post<UserDefinedTriggerDefinition, UserDefinedTriggerDefinition>(`${BASE_PATH}/triggers`, definition),
  update: (name: string, definition: UserDefinedTriggerDefinition) =>
    agent.put<UserDefinedTriggerDefinition, UserDefinedTriggerDefinition>(`${BASE_PATH}/triggers/${name}`, definition),
  unregister: (name: string) => agent.delete(`${BASE_PATH}/triggers/${name}`),
};

// Variables API
export const variablesApi = {
  getAll: () => agent.get<Record<string, UserDefinedVariableDefinition>>(`${BASE_PATH}/variables`),
  get: (name: string) => agent.get<UserDefinedVariableDefinition>(`${BASE_PATH}/variables/${name}`),
  register: (definition: UserDefinedVariableDefinition) =>
    agent.post<UserDefinedVariableDefinition, UserDefinedVariableDefinition>(`${BASE_PATH}/variables`, definition),
  update: (name: string, definition: UserDefinedVariableDefinition) =>
    agent.put<UserDefinedVariableDefinition, UserDefinedVariableDefinition>(
      `${BASE_PATH}/variables/${name}`,
      definition
    ),
  unregister: (name: string) => agent.delete(`${BASE_PATH}/variables/${name}`),
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
  getSchema: (type: BlockType) => agent.get<ModuleType>(`${BASE_PATH}/${type}/schema`),
};
