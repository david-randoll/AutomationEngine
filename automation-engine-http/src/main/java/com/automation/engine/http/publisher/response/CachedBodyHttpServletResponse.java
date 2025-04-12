package com.automation.engine.http.publisher.response;

import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.Objects.isNull;

@Slf4j
public class CachedBodyHttpServletResponse extends ContentCachingResponseWrapper {
    @Setter
    @Getter
    private Exception exception;

    public CachedBodyHttpServletResponse(HttpServletResponse response) {
        super(response);
    }

    public CompletionStage<String> getResponseBody(ContentCachingRequestWrapper request) throws IOException {
        var future = new CompletableFuture<String>();

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

    private void getBody(CompletableFuture<String> future) throws IOException {
        String body = new String(this.getContentAsByteArray(), this.getCharacterEncoding());
        this.copyBodyToResponse(); // IMPORTANT: copy response back into original response
        future.complete(body);
    }

    public boolean hasException() {
        return !isNull(exception);
    }
}