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

const BASE_PATH = "/automation-engine/user-defined";

// Actions API
export const actionsApi = {
  getAll: () => agent.get<Record<string, UserDefinedActionDefinition>>(`${BASE_PATH}/actions`),
  get: (name: string) => agent.get<UserDefinedActionDefinition>(`${BASE_PATH}/actions/${name}`),
  register: (definition: UserDefinedActionDefinition) =>
    agent.post<UserDefinedActionDefinition, UserDefinedActionDefinition>(`${BASE_PATH}/actions`, definition),
  unregister: (name: string) => agent.delete(`${BASE_PATH}/actions/${name}`),
};

// Conditions API
export const conditionsApi = {
  getAll: () => agent.get<Record<string, UserDefinedConditionDefinition>>(`${BASE_PATH}/conditions`),
  get: (name: string) => agent.get<UserDefinedConditionDefinition>(`${BASE_PATH}/conditions/${name}`),
  register: (definition: UserDefinedConditionDefinition) =>
    agent.post<UserDefinedConditionDefinition, UserDefinedConditionDefinition>(`${BASE_PATH}/conditions`, definition),
  unregister: (name: string) => agent.delete(`${BASE_PATH}/conditions/${name}`),
};

// Triggers API
export const triggersApi = {
  getAll: () => agent.get<Record<string, UserDefinedTriggerDefinition>>(`${BASE_PATH}/triggers`),
  get: (name: string) => agent.get<UserDefinedTriggerDefinition>(`${BASE_PATH}/triggers/${name}`),
  register: (definition: UserDefinedTriggerDefinition) =>
    agent.post<UserDefinedTriggerDefinition, UserDefinedTriggerDefinition>(`${BASE_PATH}/triggers`, definition),
  unregister: (name: string) => agent.delete(`${BASE_PATH}/triggers/${name}`),
};

// Variables API
export const variablesApi = {
  getAll: () => agent.get<Record<string, UserDefinedVariableDefinition>>(`${BASE_PATH}/variables`),
  get: (name: string) => agent.get<UserDefinedVariableDefinition>(`${BASE_PATH}/variables/${name}`),
  register: (definition: UserDefinedVariableDefinition) =>
    agent.post<UserDefinedVariableDefinition, UserDefinedVariableDefinition>(`${BASE_PATH}/variables`, definition),
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
};
