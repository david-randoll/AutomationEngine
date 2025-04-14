package com.automation.engine.http.publisher.response;

import com.automation.engine.http.utils.HttpServletUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Slf4j
public class CachedBodyHttpServletResponse extends ContentCachingResponseWrapper {
    private final ObjectMapper mapper;

    public CachedBodyHttpServletResponse(HttpServletResponse response, ObjectMapper mapper) {
        super(response);
        this.mapper = mapper;
    }

    public CompletionStage<JsonNode> getResponseBody(ContentCachingRequestWrapper request) throws IOException {
        var future = new CompletableFuture<JsonNode>();

        if (request.isAsyncStarted()) {
            request.getAsyncContext().addListener(new AsyncListener() {
                public void onComplete(AsyncEvent asyncEvent) throws IOException {
                    getBody(future);
                }

                public void onTimeout(AsyncEvent asyncEvent) {
                    //ignore
                }

                public void onError(AsyncEvent asyncEvent) {
                    //ignore
                    log.error("Error occurred while processing async request", asyncEvent.getThrowable());
                }

                public void onStartAsync(AsyncEvent asyncEvent) {
                    //ignore
                }
            });
        } else {
            getBody(future);
        }
        return future;
    }

    private void getBody(CompletableFuture<JsonNode> future) throws IOException {
        JsonNode body = HttpServletUtils.parseByteArrayToJsonNode(this.getContentType(), this.getContentAsByteArray(), mapper);
        future.complete(body);
    }
}