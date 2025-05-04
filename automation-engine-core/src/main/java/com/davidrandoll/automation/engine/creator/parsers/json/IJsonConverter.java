package com.davidrandoll.automation.engine.creator.parsers.json;

public interface IJsonConverter {
    <T> T convert(String json, Class<T> clazz);
}
