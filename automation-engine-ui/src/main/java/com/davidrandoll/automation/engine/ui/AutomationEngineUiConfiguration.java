package com.davidrandoll.automation.engine.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Optional;

@Configuration
@EnableConfigurationProperties(AutomationEngineUiProperties.class)
@ConditionalOnProperty(prefix = "automation-engine.ui", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class AutomationEngineUiConfiguration implements WebMvcConfigurer {

    private final AutomationEngineUiProperties properties;

    // Helper to normalize the path (Keep this)
    private String getNormalizedPath() {
        String path = Optional.ofNullable(properties.getPath()).orElse("");
        if (!path.startsWith("/"))
            path = "/" + path;
        if (path.endsWith("/"))
            path = path.substring(0, path.length() - 1);
        return path;
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        String path = getNormalizedPath();
        registry.addViewController(path)
                .setViewName("redirect:" + path + "/");

        registry.addViewController(path + "/")
                .setViewName("forward:" + path + "/index.html");

        registry.setOrder(Integer.MIN_VALUE);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String path = getNormalizedPath();

        registry.addResourceHandler(path + "/**")
                .addResourceLocations("classpath:/META-INF/automation-engine-ui/")
                .resourceChain(true)
                .addResolver(new PathResourceResolverExtension());
    }
}