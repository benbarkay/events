package com.benbarkay.events;

import java.util.function.Function;

public interface EventEmitter<T> {
    void emit(T event);
    void error(Throwable t);
    default <F> EventEmitter<F> decorate(Function<F, T> fn) {
        EventBus<F> decorator = EventBus.blocking();
        decorator.map(fn)
                .error(RuntimeException.class, (e) -> {
                    throw e;
                })
                .consume(this::emit);
        return decorator;
    }
    boolean hasSubscribers();
}
