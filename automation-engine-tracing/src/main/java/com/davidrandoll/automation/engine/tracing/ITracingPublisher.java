package com.davidrandoll.automation.engine.tracing;

@FunctionalInterface
public interface ITracingPublisher {
    void publish(ExecutionTrace executionTrace);
}
