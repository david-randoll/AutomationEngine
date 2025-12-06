package com.davidrandoll.automation.engine.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
@EnableConfigurationProperties(AutomationEngineUiProperties.class)
@ConditionalOnProperty(prefix = "automation-engine.ui", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class AutomationEngineUiConfiguration implements WebMvcConfigurer {

    private final AutomationEngineUiProperties properties;

    // Helper to normalize the path (Keep this)
    private String getNormalizedPath() {
        String path = properties.getPath();
        if (path == null) path = "";
        if (!path.startsWith("/")) path = "/" + path;
        if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
        return path;
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        String path = getNormalizedPath();
        registry.addViewController(path + "/")
                .setViewName("forward:" + path + "/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String path = getNormalizedPath();

        registry.addResourceHandler(path + "/**")
                .addResourceLocations("classpath:/META-INF/automation-engine-ui/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        // 1. ROOT PATH FIX: If the path is empty, serve index.html immediately
                        if (resourcePath.trim().isEmpty() || resourcePath.equals("/")) {
                            return location.createRelative("index.html");
                        }

                        // 2. Exact Match: Check if the exact file exists (images, css, js)
                        Resource requestedResource = location.createRelative(resourcePath);
                        // IMPORTANT: We explicitly check isReadable() to filter out directories
                        if (requestedResource.exists() && requestedResource.isReadable() && requestedResource.isFile()) {
                            return requestedResource;
                        }

                        // 3. HTML Extension: Check if file exists with .html (for /user-defined)
                        Resource htmlResource = location.createRelative(resourcePath + ".html");
                        if (htmlResource.exists() && htmlResource.isReadable()) {
                            return htmlResource;
                        }

                        Resource htmlIndexResource = location.createRelative(resourcePath + "/index.html");
                        if (htmlIndexResource.exists() && htmlIndexResource.isReadable()) {
                            return htmlIndexResource;
                        }

                        // 4. SPA Fallback: Everything else goes to index.html (for deep links /user-defined/conditions)
                        return location.createRelative("index.html");
                    }
                });
    }
}