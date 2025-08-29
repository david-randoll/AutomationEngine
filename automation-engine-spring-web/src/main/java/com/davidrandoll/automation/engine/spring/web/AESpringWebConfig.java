package com.davidrandoll.automation.engine.spring.web;

import com.davidrandoll.automation.engine.AutomationEngine;
import com.davidrandoll.automation.engine.spring.web.modules.actions.send_http_request.SendHttpRequestAction;
import com.davidrandoll.automation.engine.spring.web.modules.conditions.http_error_detail.HttpErrorDetailCondition;
import com.davidrandoll.automation.engine.spring.web.modules.conditions.http_full_path.HttpFullPathCondition;
import com.davidrandoll.automation.engine.spring.web.modules.conditions.http_header.HttpHeaderCondition;
import com.davidrandoll.automation.engine.spring.web.modules.conditions.http_latency.HttpLatencyCondition;
import com.davidrandoll.automation.engine.spring.web.modules.conditions.http_method.HttpMethodCondition;
import com.davidrandoll.automation.engine.spring.web.modules.conditions.http_path.HttpPathCondition;
import com.davidrandoll.automation.engine.spring.web.modules.conditions.http_path_param.HttpPathParamCondition;
import com.davidrandoll.automation.engine.spring.web.modules.conditions.http_query_param.HttpQueryParamCondition;
import com.davidrandoll.automation.engine.spring.web.modules.conditions.http_request_body.HttpRequestBodyCondition;
import com.davidrandoll.automation.engine.spring.web.modules.conditions.http_response_body.HttpResponseBodyCondition;
import com.davidrandoll.automation.engine.spring.web.modules.conditions.http_response_status.HttpResponseStatusCondition;
import com.davidrandoll.automation.engine.spring.web.modules.conditions.on_http_path_exists.OnHttpPathExistsCondition;
import com.davidrandoll.automation.engine.spring.web.modules.triggers.on_http_path_exists.OnHttpPathExistsTrigger;
import com.davidrandoll.automation.engine.spring.web.modules.triggers.on_http_request.OnHttpRequestTrigger;
import com.davidrandoll.automation.engine.spring.web.modules.triggers.on_http_response.*;
import com.davidrandoll.automation.engine.spring.web.modules.triggers.on_slow_http_request.OnSlowHttpRequestTrigger;
import com.davidrandoll.spring_web_captor.publisher.IWebCaptorEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AESpringWebConfig {

    @Bean
    @ConditionalOnMissingBean
    public IWebCaptorEventPublisher automationEngineEventPublisher(AutomationEngine automationEngine) {
        return new AutomationEngineEventPublisher(automationEngine);
    }

    /*
     * Actions
     */
    @Bean("sendHttpRequestAction")
    @ConditionalOnMissingBean(name = "sendHttpRequestAction", ignored = SendHttpRequestAction.class)
    @ConditionalOnClass(WebClient.class)
    public SendHttpRequestAction sendHttpRequestAction(ObjectMapper mapper) {
        return new SendHttpRequestAction(mapper);
    }

    /*
     * Conditions
     */

    @Bean("httpErrorDetailCondition")
    @ConditionalOnMissingBean(name = "httpErrorDetailCondition", ignored = HttpErrorDetailCondition.class)
    public HttpErrorDetailCondition httpErrorDetailCondition(ObjectMapper objectMapper) {
        return new HttpErrorDetailCondition(objectMapper);
    }

    @Bean("httpFullPathCondition")
    @ConditionalOnMissingBean(name = "httpFullPathCondition", ignored = HttpFullPathCondition.class)
    public HttpFullPathCondition httpFullPathCondition(ObjectMapper objectMapper) {
        return new HttpFullPathCondition(objectMapper);
    }

    @Bean(name = "httpHeaderCondition")
    @ConditionalOnMissingBean(name = "httpHeaderCondition", ignored = HttpHeaderCondition.class)
    public HttpHeaderCondition httpHeaderCondition(ObjectMapper objectMapper) {
        return new HttpHeaderCondition(objectMapper);
    }

    @Bean(name = "httpLatencyCondition")
    @ConditionalOnMissingBean(name = "httpLatencyCondition", ignored = HttpLatencyCondition.class)
    public HttpLatencyCondition httpLatencyCondition() {
        return new HttpLatencyCondition();
    }

    @Bean(name = "httpMethodCondition")
    @ConditionalOnMissingBean(name = "httpMethodCondition", ignored = HttpMethodCondition.class)
    public HttpMethodCondition httpMethodCondition(ObjectMapper objectMapper) {
        return new HttpMethodCondition(objectMapper);
    }

    @Bean(name = "httpPathCondition")
    @ConditionalOnMissingBean(name = "httpPathCondition", ignored = HttpPathCondition.class)
    public HttpPathCondition httpPathCondition(ObjectMapper objectMapper) {
        return new HttpPathCondition(objectMapper);
    }

    @Bean(name = "httpPathParamCondition")
    @ConditionalOnMissingBean(name = "httpPathParamCondition", ignored = HttpPathParamCondition.class)
    public HttpPathParamCondition httpPathParamCondition(ObjectMapper objectMapper) {
        return new HttpPathParamCondition(objectMapper);
    }

    @Bean(name = "httpQueryParamCondition")
    @ConditionalOnMissingBean(name = "httpQueryParamCondition", ignored = HttpQueryParamCondition.class)
    public HttpQueryParamCondition httpQueryParamCondition(ObjectMapper objectMapper) {
        return new HttpQueryParamCondition(objectMapper);
    }

    @Bean(name = "httpRequestBodyCondition")
    @ConditionalOnMissingBean(name = "httpRequestBodyCondition", ignored = HttpRequestBodyCondition.class)
    public HttpRequestBodyCondition httpRequestBodyCondition(ObjectMapper objectMapper) {
        return new HttpRequestBodyCondition(objectMapper);
    }

    @Bean(name = "httpResponseBodyCondition")
    @ConditionalOnMissingBean(name = "httpResponseBodyCondition", ignored = HttpResponseBodyCondition.class)
    public HttpResponseBodyCondition httpResponseBodyCondition(ObjectMapper objectMapper) {
        return new HttpResponseBodyCondition(objectMapper);
    }

    @Bean(name = "httpResponseStatusCondition")
    @ConditionalOnMissingBean(name = "httpResponseStatusCondition", ignored = HttpResponseStatusCondition.class)
    public HttpResponseStatusCondition httpResponseStatusCondition(ObjectMapper objectMapper) {
        return new HttpResponseStatusCondition(objectMapper);
    }

    @Bean(name = "httpPathExistsCondition")
    @ConditionalOnMissingBean(name = "httpPathExistsCondition", ignored = OnHttpPathExistsCondition.class)
    public OnHttpPathExistsCondition httpPathExistsCondition(OnHttpPathExistsTrigger trigger) {
        return new OnHttpPathExistsCondition(trigger);
    }

    /*
     * Triggers
     */
    @Bean(name = "onHttpPathExistsTrigger")
    @ConditionalOnMissingBean(name = "onHttpPathExistsTrigger", ignored = OnHttpPathExistsTrigger.class)
    public OnHttpPathExistsTrigger onHttpPathExistsTrigger() {
        return new OnHttpPathExistsTrigger();
    }

    @Bean(name = "onHttpRequestTrigger")
    @ConditionalOnMissingBean(name = "onHttpRequestTrigger", ignored = OnHttpRequestTrigger.class)
    public OnHttpRequestTrigger onHttpRequestTrigger(ObjectMapper objectMapper) {
        return new OnHttpRequestTrigger(objectMapper);
    }

    @Bean(name = "onHttpClientErrorResponseTrigger")
    @ConditionalOnMissingBean(name = "onHttpClientErrorResponseTrigger", ignored = OnHttpClientErrorResponseTrigger.class)
    public OnHttpClientErrorResponseTrigger onHttpClientErrorResponseTrigger(OnHttpResponseTrigger onHttpResponseTrigger) {
        return new OnHttpClientErrorResponseTrigger(onHttpResponseTrigger);
    }

    @Bean(name = "onHttpErrorResponseTrigger")
    @ConditionalOnMissingBean(name = "onHttpErrorResponseTrigger", ignored = OnHttpErrorResponseTrigger.class)
    public OnHttpErrorResponseTrigger onHttpErrorResponseTrigger(OnHttpResponseTrigger onHttpResponseTrigger) {
        return new OnHttpErrorResponseTrigger(onHttpResponseTrigger);
    }

    @Bean(name = "onHttpResponseTrigger")
    @ConditionalOnMissingBean(name = "onHttpResponseTrigger", ignored = OnHttpResponseTrigger.class)
    public OnHttpResponseTrigger onHttpResponseTrigger(ObjectMapper objectMapper) {
        return new OnHttpResponseTrigger(objectMapper);
    }

    @Bean(name = "onHttpServerErrorResponseTrigger")
    @ConditionalOnMissingBean(name = "onHttpServerErrorResponseTrigger", ignored = OnHttpServerErrorResponseTrigger.class)
    public OnHttpServerErrorResponseTrigger onHttpServerErrorResponseTrigger(OnHttpResponseTrigger onHttpResponseTrigger) {
        return new OnHttpServerErrorResponseTrigger(onHttpResponseTrigger);
    }

    @Bean(name = "onHttpSuccessResponseTrigger")
    @ConditionalOnMissingBean(name = "onHttpSuccessResponseTrigger", ignored = OnHttpSuccessResponseTrigger.class)
    public OnHttpSuccessResponseTrigger onHttpSuccessResponseTrigger(OnHttpResponseTrigger onHttpResponseTrigger) {
        return new OnHttpSuccessResponseTrigger(onHttpResponseTrigger);
    }

    @Bean(name = "onSlowHttpRequestTrigger")
    public OnSlowHttpRequestTrigger onSlowHttpRequestTrigger() {
        return new OnSlowHttpRequestTrigger();
    }

}