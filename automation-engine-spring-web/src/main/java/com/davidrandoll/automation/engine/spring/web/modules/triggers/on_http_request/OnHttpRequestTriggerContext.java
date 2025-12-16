package com.davidrandoll.automation.engine.spring.web.modules.triggers.on_http_request;

import com.davidrandoll.automation.engine.core.triggers.ITriggerContext;
import com.davidrandoll.spring_web_captor.event.HttpMethodEnum;
import com.davidrandoll.automation.engine.spring.web.jackson.flexible_method.FlexibleHttpMethodList;
import com.davidrandoll.automation.engine.spring.web.jackson.flexible_multi_value_map.FlexibleMultiValueMap;
import com.davidrandoll.automation.engine.spring.web.jackson.flexible_string_list.FlexibleStringList;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

import java.util.List;

@Data
@NoArgsConstructor
@FieldNameConstants
@JsonPropertyOrder({
        OnHttpRequestTriggerContext.Fields.alias,
        OnHttpRequestTriggerContext.Fields.description,
        OnHttpRequestTriggerContext.Fields.methods,
        OnHttpRequestTriggerContext.Fields.fullPaths,
        OnHttpRequestTriggerContext.Fields.paths,
        OnHttpRequestTriggerContext.Fields.headers,
        OnHttpRequestTriggerContext.Fields.queryParams,
        OnHttpRequestTriggerContext.Fields.pathParams,
        OnHttpRequestTriggerContext.Fields.requestBody
})
public class OnHttpRequestTriggerContext implements ITriggerContext {
    /** Unique identifier for this trigger */
    private String alias;

    /** Human-readable description of what this trigger responds to */
    private String description;

    /** HTTP methods to match (e.g., GET, POST, PUT, DELETE). If not specified, all methods will match */
    @JsonAlias({"method", "methods"})
    @FlexibleHttpMethodList
    private List<HttpMethodEnum> methods;

    /** Full URL paths to match including context path (e.g., "/api/v1/users"). Supports wildcards */
    @JsonAlias({"fullPath", "fullPaths", "fullUrl", "fullUrls"})
    @FlexibleStringList
    private List<String> fullPaths;

    /** Request paths to match excluding context path (e.g., "/users"). Supports wildcards */
    @JsonAlias({"path", "paths"})
    @FlexibleStringList
    private List<String> paths;

    /** HTTP headers that must be present in the request for this trigger to activate */
    @JsonAlias({"headers", "header"})
    @FlexibleMultiValueMap
    private HttpHeaders headers;

    /** Query parameters that must be present in the request URL for this trigger to activate */
    @JsonAlias({"query", "queryString", "queryParam", "queryParams"})
    @FlexibleMultiValueMap
    private MultiValueMap<String, String> queryParams;

    /** Path parameters extracted from the URL pattern that must match for this trigger to activate */
    @JsonAlias({"pathParams", "pathParam"})
    @FlexibleMultiValueMap
    private MultiValueMap<String, String> pathParams;

    /** Request body content that must match for this trigger to activate. Can be a JSON object or any value */
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