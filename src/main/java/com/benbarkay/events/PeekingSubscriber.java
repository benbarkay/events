package com.benbarkay.events;

import java.util.function.Consumer;

class PeekingSubscriber<T> extends AbstractForwardingSubscriber<T, T> {

    private final Consumer<T> consumer;

    PeekingSubscriber(Consumer<T> consumer, EventEmitter<T> emitter) {
        super(emitter);
        this.consumer = consumer;
    }

    @Override
    protected void forward(T event, EventEmitter<T> emitter) {
        consumer.accept(event);
        emitter.emit(event);
    }
}
