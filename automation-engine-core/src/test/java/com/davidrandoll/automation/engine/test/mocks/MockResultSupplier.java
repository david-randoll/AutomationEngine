package com.davidrandoll.automation.engine.test.mocks;

import com.davidrandoll.automation.engine.core.result.IResult;
import com.davidrandoll.automation.engine.creator.result.IResultSupplier;

import java.util.HashMap;
import java.util.Map;

public class MockResultSupplier implements IResultSupplier {
    private final Map<String, IResult> results = new HashMap<>();

    public void addResult(String name, IResult result) {
        results.put(name, result);
    }

    @Override
    public IResult getResult(String name) {
        return results.getOrDefault(name, new SimpleResult(name));
    }
}
