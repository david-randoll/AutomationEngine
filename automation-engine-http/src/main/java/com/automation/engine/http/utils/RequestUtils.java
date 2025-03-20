package com.automation.engine.http.utils;

import com.automation.engine.http.publisher.CachedBodyHttpServletRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@UtilityClass
public class RequestUtils {

    public CompletionStage<JsonNode> getResponseBody(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, ObjectMapper objectMapper) throws IOException {
        var future = new CompletableFuture<JsonNode>();

        if (request.isAsyncStarted()) {
            request.getAsyncContext().addListener(new AsyncListener() {
                public void onComplete(AsyncEvent asyncEvent) throws IOException {
                    var body = new String(response.getContentAsByteArray(), StandardCharsets.UTF_8);
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
            String body = new String(response.getContentAsByteArray(), StandardCharsets.UTF_8);
            response.copyBodyToResponse(); // IMPORTANT: copy response back into original response
            try {
                future.complete(objectMapper.readTree(body));
            } catch (IOException e) {
                future.complete(objectMapper.nullNode());
            }
        }
        return future;
    }
}

