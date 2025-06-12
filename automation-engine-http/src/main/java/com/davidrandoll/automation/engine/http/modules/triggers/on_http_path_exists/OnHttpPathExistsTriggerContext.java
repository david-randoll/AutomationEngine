package com.davidrandoll.automation.engine.http.modules.triggers.on_http_path_exists;

import com.davidrandoll.automation.engine.core.triggers.ITriggerContext;
import com.davidrandoll.spring_web_captor.event.HttpMethodEnum;
import com.davidrandoll.automation.engine.http.jackson.flexible_method.FlexibleHttpMethodList;
import com.davidrandoll.automation.engine.http.jackson.flexible_string_list.FlexibleStringList;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnHttpPathExistsTriggerContext implements ITriggerContext {
    private String alias;

    @JsonAlias({"path", "paths", "url", "fullPath"})
    @FlexibleStringList
    private List<String> paths;

    @JsonAlias({"method", "methods"})
    @FlexibleHttpMethodList
    private List<HttpMethodEnum> methods;
}