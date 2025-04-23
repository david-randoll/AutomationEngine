package com.automation.engine.http.publisher.request;

import com.automation.engine.http.event.HttpMethodEnum;
import com.automation.engine.http.event.HttpRequestEvent;
import com.automation.engine.http.utils.HttpServletUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.*;
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

    private HttpRequestEvent httpRequestEvent;
    private final ObjectMapper objectMapper;

    public CachedBodyHttpServletRequest(HttpServletRequest request, ObjectMapper objectMapper) throws IOException {
        super(request);
        InputStream requestInputStream = request.getInputStream();
        this.cachedBody = StreamUtils.copyToByteArray(requestInputStream);
        this.objectMapper = objectMapper;
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
    public JsonNode getBody() {
        return HttpServletUtils.parseByteArrayToJsonNode(this.getContentType(), this.cachedBody, objectMapper);
    }

    public MultiValueMap<String, String> getRequestParams() {
        return this.getParameterMap()
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue() != null ? Arrays.asList(entry.getValue()) : Collections.emptyList(),
                        (a, b) -> b,
                        LinkedMultiValueMap::new
                ));
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getPathVariables() {
        var pathVariables = this.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables instanceof Map<?, ?>) {
            return (Map<String, String>) pathVariables;
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
        if (this.httpRequestEvent != null) {
            return this.httpRequestEvent;
        }
        this.httpRequestEvent = HttpRequestEvent.builder()
                .endpointExists(this.endpointExists)
                .fullUrl(this.getFullUrl())
                .path(this.getPath())
                .method(HttpMethodEnum.fromValue(this.getMethod()))
                .headers(this.getHttpHeaders())
                .queryParams(this.getRequestParams())
                .pathParams(this.getPathVariables())
                .requestBody(this.getBody())
                .build();

        return this.httpRequestEvent;
    }
}
