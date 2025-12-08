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
import java.util.List;
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
        if (!path.startsWith("/")) path = "/" + path;
        if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
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
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        if (resourcePath.matches(".*\\.[a-zA-Z0-9]+$")) {
                            Resource staticFile = location.createRelative(resourcePath);
                            if (staticFile.exists() && staticFile.isReadable()) {
                                return staticFile;
                            }

                            // asset not found. react store all it's asset in an assets folder
                            // modify the path to point to the assets folder. replace any path before /assets/ with /assets/
                            int assetsIndex = resourcePath.indexOf("/assets/");
                            if (assetsIndex != -1) {
                                String assetsPath = resourcePath.substring(assetsIndex);
                                Resource assetFile = location.createRelative(assetsPath);
                                if (assetFile.exists() && assetFile.isReadable()) {
                                    return assetFile;
                                }
                            }

                            // if manifest.json or favicon.ico then this is in root path
                            String fileName = resourcePath.substring(resourcePath.lastIndexOf("/") + 1);
                            if (List.of("manifest.json", "favicon.ico").contains(fileName)) {
                                Resource rootFile = location.createRelative(fileName);
                                if (rootFile.exists() && rootFile.isReadable()) {
                                    return rootFile;
                                }
                            }

                            return null;
                        }

                        // --- 2) Try directory-based index.html (e.g. /ui/user-defined) ---
                        Resource htmlIndex = location.createRelative(resourcePath + "/index.html");
                        if (htmlIndex.exists() && htmlIndex.isReadable()) {
                            return htmlIndex;
                        }

                        // --- 3) Try HTML extension (/ui/user-defined → user-defined.html) ---
                        Resource htmlFile = location.createRelative(resourcePath + ".html");
                        if (htmlFile.exists() && htmlFile.isReadable()) {
                            return htmlFile;
                        }

                        return location.createRelative("index.html");
                    }
                });
    }
}