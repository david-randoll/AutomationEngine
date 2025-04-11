package com.automation.engine.creator.parsers.yaml;

public interface IYamlConverter {
    <T> T convert(String yaml, Class<T> clazz);
}