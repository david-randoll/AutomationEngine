package com.davidrandoll.automation.engine.creator.result;

import com.davidrandoll.automation.engine.core.result.IResult;

public interface IResultSupplier {
    IResult getResult(String name);
}