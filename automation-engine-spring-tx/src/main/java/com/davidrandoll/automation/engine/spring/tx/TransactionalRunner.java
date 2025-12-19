package com.davidrandoll.automation.engine.spring.tx;

import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

public class TransactionalRunner {

    @Transactional
    public <T> T runInTransaction(Supplier<T> supplier) {
        return supplier.get();
    }
}
