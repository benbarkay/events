package com.benbarkay.events;

class ForwardingSubscriber<T> extends AbstractForwardingSubscriber<T, T> {
    ForwardingSubscriber(EventEmitter<T> emitter) {
        super(emitter);
    }

    @Override
    protected void forward(T event, EventEmitter<T> emitter) {
        emitter.emit(event);
    }
}
