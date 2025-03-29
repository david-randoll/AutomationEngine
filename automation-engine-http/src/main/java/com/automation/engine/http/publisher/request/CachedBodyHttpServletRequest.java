package com.automation.engine.http.publisher.request;

import com.automation.engine.http.event.HttpMethodEnum;
import com.automation.engine.http.event.HttpRequestEvent;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class CachedBodyHttpServletRequest extends ContentCachingRequestWrapper {
    private final byte[] cachedBody;
    @Getter
    @Setter
    private boolean endpointExists;

    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        InputStream requestInputStream = request.getInputStream();
        this.cachedBody = StreamUtils.copyToByteArray(requestInputStream);
    }

    @Override
    @NonNull
    public ServletInputStream getInputStream() throws IOException {
        return new CachedBodyServletInputStream(this.cachedBody);
    }

    @Override
    @NonNull
    public BufferedReader getReader() throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
        return new BufferedReader(new InputStreamReader(byteArrayInputStream));
    }

    @SneakyThrows
    public String getBody() {
        return IOUtils.toString(this.getInputStream(), StandardCharsets.UTF_8);
    }

    public Map<String, Object> getRequestParams() {
        return this.getParameterMap()
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Arrays.asList(e.getValue())
                ));
    }

    public Map<String, Object> getPathVariables() {
        var pathVariables = this.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables instanceof Map<?, ?>) {
            return (Map<String, Object>) pathVariables;
        }
        return Collections.emptyMap();
    }

    public Map<String, ArrayList<String>> getHeaders() {
        return Collections.list(this.getHeaderNames()).stream()
                .collect(Collectors.toMap(
                        h -> h,
                        h -> Collections.list(this.getHeaders(h))
                ));
    }

    public HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(this.getHeaders());
        return headers;
    }

    public String getPath() {
        return this.getRequestURI();
    }

    public String getFullUrl() {
        return this.getRequestURL().toString();
    }

    public HttpRequestEvent toHttpRequestEvent() {
        return new HttpRequestEvent(
                this.getFullUrl(),
                this.getPath(),
                HttpMethodEnum.fromValue(this.getMethod()),
                this.getHttpHeaders(),
                this.getRequestParams(),
                this.getPathVariables(),
                this.getBody()
        );
    }
}
