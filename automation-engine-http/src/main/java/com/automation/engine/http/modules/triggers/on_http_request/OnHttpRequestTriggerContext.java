package com.automation.engine.http.modules.triggers.on_http_request;

import com.automation.engine.core.triggers.ITriggerContext;
import com.automation.engine.http.event.HttpMethodEnum;
import com.automation.engine.http.jackson.flexible_method.FlexibleHttpMethodList;
import com.automation.engine.http.jackson.flexible_multi_value_map.FlexibleMultiValueMap;
import com.automation.engine.http.jackson.flexible_string_list.FlexibleStringList;
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

    @JsonAlias({"method", "methods"})
    @FlexibleHttpMethodList
    private List<HttpMethodEnum> methods;

    @JsonAlias({"fullPath", "fullPaths", "fullUrl", "fullUrls"})
    @FlexibleStringList
    private List<String> fullPaths;

    @JsonAlias({"path", "paths"})
    @FlexibleStringList
    private List<String> paths;

    @JsonAlias({"headers", "header"})
    @FlexibleMultiValueMap
    private HttpHeaders headers;

    @JsonAlias({"query", "queryString", "queryParam", "queryParams"})
    @FlexibleMultiValueMap
    private MultiValueMap<String, String> queryParams;

    @JsonAlias({"pathParams", "pathParam", "path"})
    @FlexibleMultiValueMap
    private MultiValueMap<String, String> pathParams;

    public boolean hasMethods() {
        return methods != null && !methods.isEmpty();
    }

    public boolean hasFullPaths() {
        return fullPaths != null && !fullPaths.isEmpty();
    }

    public boolean hasPaths() {
        return paths != null && !paths.isEmpty();
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