// API functions for automation playground

import { agent, getApiPath } from "./agent";
import type { ExecuteAutomationRequest, ExecuteAutomationResponse } from "@/types/trace";

function getBasePath(): string {
  return `${getApiPath()}/playground`;
}

/**
 * Playground API for executing automations with tracing.
 */
export const playgroundApi = {
  /**
   * Execute an automation with the provided definition and inputs.
   * @param request - The execution request containing automation definition and inputs
   * @returns The execution response with result and trace
   */
  execute: (request: ExecuteAutomationRequest) =>
    agent.post<ExecuteAutomationResponse, ExecuteAutomationRequest>(`${getBasePath()}/execute`, request),

  /**
   * Execute an automation, returning raw API response.
   * @param request - The execution request containing automation definition and inputs
   * @returns The raw API response with execution result and trace
   */
  executeHttp: (request: ExecuteAutomationRequest) =>
    agent.postHttp<ExecuteAutomationResponse, ExecuteAutomationRequest>(`${getBasePath()}/execute`, request),
};
