// agent.ts

export type HttpMethod = "GET" | "POST" | "PUT" | "DELETE" | "PATCH";

declare global {
  interface Window {
    __APP_CONFIG__: {
      contextPath?: string;
    };
  }
}

export interface ApiResponse<T = unknown> {
  success: boolean;
  data?: T;
  error?: {
    status: number;
    message: string;
  };
}

export interface RequestOptions<T = unknown> {
  method?: HttpMethod;
  headers?: Record<string, string>;
  body?: T;
}

export function getContextPath(): string {
  if (typeof window !== "undefined") {
    return window.__APP_CONFIG__?.contextPath || "";
  }
  return "";
}

export function getApiBaseUrl(): string {
  let baseUrl = "";
  if (typeof window !== "undefined") {
    console.log("Determining API base URL from window location");
    const contextPath = getContextPath();
    baseUrl = `${window.location.origin}/${contextPath}`.replace(/([^:]\/)\/+/g, "$1");
  }
  console.log("API Base URL:", baseUrl);
  return baseUrl;
}

async function requestHttp<TResponse = unknown, TBody = unknown>(
  endpoint: string,
  options: RequestOptions<TBody> = {}
): Promise<ApiResponse<TResponse>> {
  const { method = "GET", headers = {}, body } = options;

  try {
    const res = await fetch(`${getApiBaseUrl()}${endpoint}`, {
      method,
      headers: {
        "Content-Type": "application/json",
        ...headers,
      },
      body: body ? JSON.stringify(body) : undefined,
    });

    if (!res.ok) {
      const errorText = await res.text();
      return {
        success: false,
        error: {
          status: res.status,
          message: errorText || res.statusText,
        },
      };
    }

    // try parsing JSON, fallback to text
    const contentType = res.headers.get("content-type");
    const parsed = contentType?.includes("application/json") ? await res.json() : await res.text();
    return {
      success: true,
      data: parsed as TResponse,
    };
  } catch (err: unknown) {
    const errorMessage = err instanceof Error ? err.message : "Unknown error";
    return {
      success: false,
      error: {
        status: 0,
        message: errorMessage,
      },
    };
  }
}

/**
 * Wrapper that unwraps ApiResponse<T>, throwing on error
 */
async function request<TResponse = unknown, TBody = unknown>(
  endpoint: string,
  options: RequestOptions<TBody> = {}
): Promise<TResponse> {
  const res = await requestHttp<TResponse, TBody>(endpoint, options);
  if (!res.success) {
    throw new Error(`HTTP error! status: ${res.error?.status}, message: ${res.error?.message}`);
  }
  return res.data as TResponse;
}

export const agent = {
  // GET
  getHttp: <T>(endpoint: string, headers?: Record<string, string>) =>
    requestHttp<T>(endpoint, { method: "GET", headers }),
  get: <T>(endpoint: string, headers?: Record<string, string>) => request<T>(endpoint, { method: "GET", headers }),

  // POST
  postHttp: <T, B>(endpoint: string, body: B, headers?: Record<string, string>) =>
    requestHttp<T, B>(endpoint, { method: "POST", body, headers }),
  post: <T, B>(endpoint: string, body: B, headers?: Record<string, string>) =>
    request<T, B>(endpoint, { method: "POST", body, headers }),

  // PUT
  putHttp: <T, B>(endpoint: string, body: B, headers?: Record<string, string>) =>
    requestHttp<T, B>(endpoint, { method: "PUT", body, headers }),
  put: <T, B>(endpoint: string, body: B, headers?: Record<string, string>) =>
    request<T, B>(endpoint, { method: "PUT", body, headers }),

  // PATCH
  patchHttp: <T, B>(endpoint: string, body: B, headers?: Record<string, string>) =>
    requestHttp<T, B>(endpoint, { method: "PATCH", body, headers }),
  patch: <T, B>(endpoint: string, body: B, headers?: Record<string, string>) =>
    request<T, B>(endpoint, { method: "PATCH", body, headers }),

  // DELETE
  deleteHttp: <T>(endpoint: string, headers?: Record<string, string>) =>
    requestHttp<T>(endpoint, { method: "DELETE", headers }),
  delete: <T>(endpoint: string, headers?: Record<string, string>) =>
    request<T>(endpoint, { method: "DELETE", headers }),
};
