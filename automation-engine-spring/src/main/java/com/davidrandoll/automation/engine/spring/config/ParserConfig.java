package com.davidrandoll.automation.engine.spring.config;

import com.davidrandoll.automation.engine.creator.parsers.AutomationParserRouter;
import com.davidrandoll.automation.engine.creator.parsers.IAutomationFormatParser;
import com.davidrandoll.automation.engine.creator.parsers.ManualAutomationBuilder;
import com.davidrandoll.automation.engine.creator.parsers.json.IJsonConverter;
import com.davidrandoll.automation.engine.creator.parsers.json.JsonAutomationParser;
import com.davidrandoll.automation.engine.creator.parsers.yaml.IYamlConverter;
import com.davidrandoll.automation.engine.creator.parsers.yaml.YamlAutomationParser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class ParserConfig {

    @Bean("jsonAutomationParser")
    @ConditionalOnMissingBean
    public JsonAutomationParser jsonAutomationParser(ManualAutomationBuilder builder, IJsonConverter converter) {
        return new JsonAutomationParser(builder, converter);
    }

    @Bean("yamlAutomationParser")
    @ConditionalOnMissingBean
    public YamlAutomationParser yamlAutomationParser(ManualAutomationBuilder builder, IYamlConverter converter) {
        return new YamlAutomationParser(builder, converter);
    }

    @Bean
    @ConditionalOnMissingBean
    public AutomationParserRouter automationParserRouter(Map<String, IAutomationFormatParser<?>> formatParsers) {
        return new AutomationParserRouter(formatParsers);
    }
}