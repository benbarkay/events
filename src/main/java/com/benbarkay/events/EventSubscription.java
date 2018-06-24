package com.benbarkay.events;

import java.util.function.Consumer;

public interface EventSubscription<T> {
    <E extends Throwable> EventSubscription<T> error(Class<E> type, Consumer<E> consumer);
    default EventSubscription<T> error(Consumer<Throwable> consumer) {
        return error(Throwable.class, consumer);
    }
    boolean cancel();
}
