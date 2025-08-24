// agent.ts

export type HttpMethod = "GET" | "POST" | "PUT" | "DELETE" | "PATCH";

export interface RequestOptions<T = any> {
    method?: HttpMethod;
    headers?: Record<string, string>;
    body?: T;
}

const API_BASE_URL = process.env.NEXT_PUBLIC_BASE_URL || "";

async function request<TResponse = any, TBody = any>(
    endpoint: string,
    options: RequestOptions<TBody> = {}
): Promise<TResponse> {
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
            throw new Error(`HTTP error! status: ${res.status}, message: ${errorText}`);
        }

        // try parsing JSON, fallback to text
        const contentType = res.headers.get("content-type");
        if (contentType && contentType.includes("application/json")) {
            return (await res.json()) as TResponse;
        } else {
            return (await res.text()) as TResponse;
        }
    } catch (err) {
        console.error("HTTP Request failed:", err);
        throw err;
    }
}

export const agent = {
    get: <T>(endpoint: string, headers?: Record<string, string>) => request<T>(endpoint, { method: "GET", headers }),

    post: <T, B>(endpoint: string, body: B, headers?: Record<string, string>) =>
        request<T, B>(endpoint, { method: "POST", body, headers }),

    put: <T, B>(endpoint: string, body: B, headers?: Record<string, string>) =>
        request<T, B>(endpoint, { method: "PUT", body, headers }),

    patch: <T, B>(endpoint: string, body: B, headers?: Record<string, string>) =>
        request<T, B>(endpoint, { method: "PATCH", body, headers }),

    delete: <T>(endpoint: string, headers?: Record<string, string>) =>
        request<T>(endpoint, { method: "DELETE", headers }),
};
