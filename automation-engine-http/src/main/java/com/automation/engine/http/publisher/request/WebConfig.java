package com.automation.engine.http.publisher.request;

import com.automation.engine.http.publisher.response.RuntimeExceptionResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration("webConfig")
@EnableWebMvc
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "webConfig", ignored = WebConfig.class)
public class WebConfig implements WebMvcConfigurer {
    private final HttpRequestEventPublisher httpRequestEventPublisher;
    private final RuntimeExceptionResolver runtimeExceptionResolver;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(httpRequestEventPublisher).addPathPatterns("/**");
    }

    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.add(runtimeExceptionResolver);
    }
}
