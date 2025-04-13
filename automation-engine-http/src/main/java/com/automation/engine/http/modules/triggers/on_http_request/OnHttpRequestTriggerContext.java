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
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnHttpRequestTriggerContext implements ITriggerContext {
    private String alias;
    private List<HttpMethodEnum> methods;
    private List<String> fullPaths;
    private List<String> paths;
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
        this.methods = switch (value) {
            case String s -> List.of(HttpMethodEnum.fromValue(s));
            case List<?> list -> list.stream()
                    .map(String::valueOf)
                    .map(HttpMethodEnum::fromValue)
                    .toList();
            default -> throw new IllegalArgumentException("Unsupported type for methods: " + value.getClass());
        };
    }

    public boolean hasFullPaths() {
        return fullPaths != null && !fullPaths.isEmpty();
    }

    @JsonAlias({"fullPath", "fullPaths", "fullUrl", "fullUrls"})
    public void setFullPaths(Object value) {
        this.fullPaths = switch (value) {
            case String s -> regexReplaceMatchingPathParams(List.of(s));
            case List<?> list -> regexReplaceMatchingPathParams(list);
            default -> throw new IllegalArgumentException("Unsupported type for fullPaths: " + value.getClass());
        };
    }

    public boolean hasPaths() {
        return paths != null && !paths.isEmpty();
    }

    @JsonAlias({"path", "paths"})
    public void setPaths(Object value) {
        this.paths = switch (value) {
            case String s -> regexReplaceMatchingPathParams(List.of(s));
            case List<?> list -> regexReplaceMatchingPathParams(list);
            default -> throw new IllegalArgumentException("Unsupported type for paths: " + value.getClass());
        };
    }

    private static List<String> regexReplaceMatchingPathParams(List<?> list) {
        return list.stream()
                .map(String::valueOf)
                // match any dynamic path param. for example, /api/v1/{id} will be replaced with /api/v1/.*
                .map(path -> path.replaceAll("\\{[^}]+}", ".*"))
                //replace tail slash with empty string
                .map(path -> path.replaceAll("/$", ""))
                .toList();
    }

    public boolean hasHeaders() {
        return headers != null && !headers.isEmpty();
    }

    @JsonAlias({"headers", "header"})
    public void setHeaders(Object value) {
        this.headers = switch (value) {
            case HttpHeaders h -> h;
            case MultiValueMap<?, ?> m -> {
                HttpHeaders httpHeaders = new HttpHeaders();
                m.forEach((key, values) -> {
                    if (values != null) {
                        for (Object value1 : values) {
                            httpHeaders.add(String.valueOf(key), String.valueOf(value1));
                        }
                    }
                });
                yield httpHeaders;
            }
            case Map<?, ?> m -> {
                HttpHeaders httpHeaders = new HttpHeaders();
                m.forEach((key, mvalue) -> {
                    if (mvalue != null) {
                        if (mvalue instanceof List<?> values) {
                            for (Object value1 : values) {
                                httpHeaders.add(String.valueOf(key), String.valueOf(value1));
                            }
                        } else {
                            httpHeaders.add(String.valueOf(key), String.valueOf(mvalue));
                        }
                    }
                });
                yield httpHeaders;
            }
            default -> throw new IllegalArgumentException("Unsupported type for headers: " + value.getClass());
        };
    }


    public boolean hasQueryParams() {
        return queryParams != null && !queryParams.isEmpty();
    }

    public boolean hasPathParams() {
        return pathParams != null && !pathParams.isEmpty();
    }
}