package com.benbarkay.events;

import java.util.function.Predicate;

class FilteringSubscriber<T> extends AbstractForwardingSubscriber<T, T> {

    private final Predicate<T> predicate;

    FilteringSubscriber(Predicate<T> predicate, EventEmitter<T> recipient) {
        super(recipient);
        this.predicate = predicate;
    }

    @Override
    protected void forward(T event, EventEmitter<T> emitter) {
        if (predicate.test(event)) {
            emitter.emit(event);
        }
    }
}
