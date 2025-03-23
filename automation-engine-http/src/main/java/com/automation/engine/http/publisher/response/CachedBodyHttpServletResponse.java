package com.automation.engine.http.publisher.response;

import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class CachedBodyHttpServletResponse extends ContentCachingResponseWrapper {
    public CachedBodyHttpServletResponse(HttpServletResponse response) {
        super(response);
    }

    public CompletionStage<String> getResponseBody(ContentCachingRequestWrapper request) throws IOException {
        var future = new CompletableFuture<String>();

        if (request.isAsyncStarted()) {
            request.getAsyncContext().addListener(new AsyncListener() {
                public void onComplete(AsyncEvent asyncEvent) throws IOException {
                    var body = new String(CachedBodyHttpServletResponse.super.getContentAsByteArray(), StandardCharsets.UTF_8);
                    CachedBodyHttpServletResponse.super.copyBodyToResponse(); // IMPORTANT: copy response back into original response
                    future.complete(body);
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
            String body = new String(this.getContentAsByteArray(), StandardCharsets.UTF_8);
            this.copyBodyToResponse(); // IMPORTANT: copy response back into original response
            future.complete(body);
        }
        return future;
    }
}
