package com.benbarkay.events;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

class EventBusSubscription<T> implements EventSubscription<T> {

    private final EventSubscriber<T> subscriber;
    private final Executor queue;
    private final EventBus<T> bus;
    private final AtomicBoolean cancelled;
    private final ErrorHandler handler;

    EventBusSubscription(
            EventSubscriber<T> subscriber,
            Executor queue,
            EventBus<T> bus,
            ErrorHandler handler) {
        this.subscriber = subscriber;
        this.queue = queue;
        this.bus = bus;
        this.handler = handler;
        cancelled = new AtomicBoolean(false);

    }

    void emit(T event) {
        if (cancelled.get()) {
            throw new IllegalStateException("attempt to emit to a cancelled subscription");
        }
        queue.execute(() -> {
            try {
                subscriber.accept(event, this);
            } catch (Throwable t) {
                handler.error(t);
            }
        });
    }

    @Override
    public <E extends Throwable> EventSubscription<T> error(Class<E> type, Consumer<E> consumer) {
        //noinspection unchecked
        handler.error(type, consumer);
        return this;
    }

    @Override
    public boolean cancel() {
        bus.unsubscribe(this);
        return true;
    }
}
