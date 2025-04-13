package com.automation.engine.http.modules.triggers.on_http_request;

import com.automation.engine.core.triggers.ITriggerContext;
import com.automation.engine.http.event.HttpMethodEnum;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnHttpRequestTriggerContext implements ITriggerContext {
    private String alias;

    private List<HttpMethodEnum> methods;

    @JsonAlias({"fullPath", "fullPaths"})
    private List<String> fullPaths;

    @JsonAlias({"path", "paths"})
    private List<String> paths;

    @JsonAlias({"headers", "header"})
    private HttpHeaders headers;

    @JsonAlias({"query", "queryString", "queryParam", "queryParams"})
    private MultiValueMap<String, String> queryParams;

    @JsonAlias({"pathParams", "pathParam", "path"})
    private MultiValueMap<String, String> pathParams;

    public boolean hasMethods() {
        return methods != null && !methods.isEmpty();
    }

    @JsonAlias({"method", "methods"})
    public void setMethods(Object value) {
        switch (value) {
            case String s -> this.methods = List.of(HttpMethodEnum.fromValue(s));
            case List<?> list -> this.methods = list.stream()
                    .map(String::valueOf)
                    .map(HttpMethodEnum::fromValue)
                    .toList();
            default -> throw new IllegalArgumentException("Unsupported type for methods: " + value.getClass());
        }
    }

    public boolean hasFullPaths() {
        return fullPaths != null && !fullPaths.isEmpty();
    }

    public List<String> getFullPathsAsRegex() {
        return this.getPaths().stream()
                .map(path -> path.replaceAll("\\{[^}]+}", ".*"))
                .toList();
    }

    public boolean hasPaths() {
        return paths != null && !paths.isEmpty();
    }

    public List<String> getPathsAsRegex() {
        return this.getPaths().stream()
                .map(path -> path.replaceAll("\\{[^}]+}", ".*"))
                .toList();
    }

    public boolean hasHeaders() {
        return headers != null && !headers.isEmpty();
    }

    public boolean hasQueryParams() {
        return queryParams != null && !queryParams.isEmpty();
    }

    public boolean hasPathParams() {
        return pathParams != null && !pathParams.isEmpty();
    }
}