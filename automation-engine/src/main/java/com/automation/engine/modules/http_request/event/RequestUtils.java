package com.automation.engine.modules.http_request.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@UtilityClass
public class RequestUtils {
    public Map<String, Object> getRequestParams(@NonNull ContentCachingRequestWrapper request) {
        return request.getParameterMap()
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Arrays.asList(e.getValue())
                ));
    }

    public Map<String, Object> getPathVariables(@NonNull ContentCachingRequestWrapper request) {
        var pathVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables instanceof Map<?, ?>) {
            return (Map<String, Object>) pathVariables;
        }
        return Collections.emptyMap();
    }

    public JsonNode getRequestBody(@NonNull ContentCachingRequestWrapper request, ObjectMapper objectMapper) {
        try {
            return objectMapper.readTree(request.getContentAsByteArray());
        } catch (IOException e) {
            return objectMapper.nullNode();
        }
    }

    public CompletionStage<JsonNode> getResponseBody(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, ObjectMapper objectMapper) throws IOException {
        var future = new CompletableFuture<JsonNode>();

        if (request.isAsyncStarted()) {
            request.getAsyncContext().addListener(new AsyncListener() {
                public void onComplete(AsyncEvent asyncEvent) throws IOException {
                    byte[] body = response.getContentAsByteArray();
                    response.copyBodyToResponse(); // IMPORTANT: copy response back into original response
                    try {
                        future.complete(objectMapper.readTree(body));
                    } catch (IOException e) {
                        future.complete(objectMapper.nullNode());
                    }
                }

                public void onTimeout(AsyncEvent asyncEvent) {
                    //ignore
                }

                public void onError(AsyncEvent asyncEvent) {
                    //ignore
                }

                public void onStartAsync(AsyncEvent asyncEvent) {
                    //ignore
                }
            });
        } else {
            byte[] body = response.getContentAsByteArray();
            response.copyBodyToResponse(); // IMPORTANT: copy response back into original response
            try {
                future.complete(objectMapper.readTree(body));
            } catch (IOException e) {
                future.complete(objectMapper.nullNode());
            }
        }
        return future;
    }

    public Map<String, ArrayList<String>> getHeaders(@NonNull ContentCachingRequestWrapper request) {
        return Collections.list(request.getHeaderNames()).stream()
                .collect(Collectors.toMap(
                        h -> h,
                        h -> Collections.list(request.getHeaders(h))
                ));
    }
}

