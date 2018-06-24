package com.benbarkay.events;

import java.util.function.Function;

class MappingSubscriber<T,F> extends AbstractForwardingSubscriber<T,F> {
    private final Function<T,F> fn;

    MappingSubscriber(Function<T, F> fn, EventEmitter<F> recipient) {
        super(recipient);
        this.fn = fn;
    }

    @Override
    protected void forward(T event, EventEmitter<F> emitter) {
        emitter.emit(fn.apply(event));
    }
}
