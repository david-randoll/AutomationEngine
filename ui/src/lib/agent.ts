// agent.ts

export type HttpMethod = "GET" | "POST" | "PUT" | "DELETE" | "PATCH";

export interface ApiResponse<T = any> {
    success: boolean;
    data?: T;
    error?: {
        status: number;
        message: string;
    };
}

export interface RequestOptions<T = any> {
    method?: HttpMethod;
    headers?: Record<string, string>;
    body?: T;
}

const API_BASE_URL = process.env.NEXT_PUBLIC_BASE_URL;
if (!API_BASE_URL) {
    console.warn("NEXT_PUBLIC_BASE_URL is not set. API requests may fail.");
}

async function requestHttp<TResponse = any, TBody = any>(
    endpoint: string,
    options: RequestOptions<TBody> = {}
): Promise<ApiResponse<TResponse>> {
    const { method = "GET", headers = {}, body } = options;

    try {
        const res = await fetch(`${API_BASE_URL}${endpoint}`, {
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
    } catch (err: any) {
        return {
            success: false,
            error: {
                status: 0,
                message: err?.message || "Unknown error",
            },
        };
    }
}

/**
 * Wrapper that unwraps ApiResponse<T>, throwing on error
 */
async function request<TResponse = any, TBody = any>(
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
