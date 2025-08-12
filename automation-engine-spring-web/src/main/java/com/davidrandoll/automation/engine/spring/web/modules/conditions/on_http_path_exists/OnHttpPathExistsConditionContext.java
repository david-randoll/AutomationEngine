package com.davidrandoll.automation.engine.spring.web.modules.conditions.on_http_path_exists;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.davidrandoll.automation.engine.spring.web.jackson.flexible_method.FlexibleHttpMethodList;
import com.davidrandoll.automation.engine.spring.web.jackson.flexible_string_list.FlexibleStringList;
import com.davidrandoll.spring_web_captor.event.HttpMethodEnum;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.List;

@Data
@NoArgsConstructor
@FieldNameConstants
@JsonPropertyOrder({
        OnHttpPathExistsConditionContext.Fields.alias,
        OnHttpPathExistsConditionContext.Fields.description,
        OnHttpPathExistsConditionContext.Fields.paths,
        OnHttpPathExistsConditionContext.Fields.methods
})
public class OnHttpPathExistsConditionContext implements IConditionContext {
    private String alias;
    private String description;

    @JsonAlias({"path", "paths", "url", "fullPath"})
    @FlexibleStringList
    private List<String> paths;

    @JsonAlias({"method", "methods"})
    @FlexibleHttpMethodList
    private List<HttpMethodEnum> methods;
}