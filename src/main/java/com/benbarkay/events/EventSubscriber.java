package com.benbarkay.events;

public interface EventSubscriber<T> {
    void accept(T event, EventSubscription subscription);
}
