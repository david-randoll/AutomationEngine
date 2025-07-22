package com.davidrandoll.automation.engine.http.modules.triggers.on_http_request;

import com.davidrandoll.automation.engine.core.triggers.ITriggerContext;
import com.davidrandoll.spring_web_captor.event.HttpMethodEnum;
import com.davidrandoll.automation.engine.http.jackson.flexible_method.FlexibleHttpMethodList;
import com.davidrandoll.automation.engine.http.jackson.flexible_multi_value_map.FlexibleMultiValueMap;
import com.davidrandoll.automation.engine.http.jackson.flexible_string_list.FlexibleStringList;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

import java.util.List;

@Data
@NoArgsConstructor
public class OnHttpRequestTriggerContext implements ITriggerContext {
    private String alias;
    private String description;

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

    @JsonAlias({"pathParams", "pathParam"})
    @FlexibleMultiValueMap
    private MultiValueMap<String, String> pathParams;

    @JsonAlias({"body", "requestBody"})
    private JsonNode requestBody;

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

    public boolean hasRequestBody() {
        return requestBody != null && !requestBody.isEmpty();
    }
}