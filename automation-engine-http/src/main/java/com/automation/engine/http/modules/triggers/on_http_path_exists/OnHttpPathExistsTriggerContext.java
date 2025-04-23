package com.automation.engine.http.modules.triggers.on_http_path_exists;

import com.automation.engine.core.triggers.ITriggerContext;
import com.automation.engine.http.event.HttpMethodEnum;
import com.automation.engine.http.jackson.flexible_method.FlexibleHttpMethodList;
import com.automation.engine.http.jackson.flexible_string_list.FlexibleStringList;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class OnHttpPathExistsTriggerContext implements ITriggerContext {
    private String alias;

    @JsonAlias({"path", "paths", "url", "fullPath"})
    @FlexibleStringList
    private List<String> paths;

    @JsonAlias({"method", "methods"})
    @FlexibleHttpMethodList
    private List<HttpMethodEnum> methods;
}