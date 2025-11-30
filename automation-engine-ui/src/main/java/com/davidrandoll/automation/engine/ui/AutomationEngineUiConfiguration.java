package com.davidrandoll.automation.engine.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(AutomationEngineUiProperties.class)
@ConditionalOnProperty(prefix = "automation-engine.ui", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class AutomationEngineUiConfiguration implements WebMvcConfigurer {

    private final AutomationEngineUiProperties properties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String path = properties.getPath();
        // Ensure path starts with / and doesn't end with /
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        registry.addResourceHandler(path + "/**")
                .addResourceLocations("classpath:/META-INF/automation-engine-ui/")
                .resourceChain(false);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        String path = properties.getPath();
        // Ensure path starts with / and doesn't end with /
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        // Forward root path to index.html
        registry.addViewController(path).setViewName("forward:" + path + "/index.html");
        registry.addViewController(path + "/").setViewName("forward:" + path + "/index.html");
    }
}

