package com.benbarkay.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class EventBus<T> implements EventSource<T>, EventEmitter<T> {

    public static <T> EventBus<T> create(Executor executor) {
        return new EventBus<>(executor);
    }

    public static <T> EventBus<T> blocking() {
        return new EventBus<>(Runnable::run);
    }

    private static final Consumer<Throwable> DEFAULT_EXCEPTION_HANDLER = (t) -> new RuntimeException(
            "Uncaught exception: " + t.getMessage(), t)
            .printStackTrace();

    private final Executor queue;
    private final List<EventBusSubscription<T>> subscriptions;
    private final ErrorHandler errorHandler;

    private EventBus(Executor queue) {
        this.queue = queue;
        subscriptions = new ArrayList<>();
        errorHandler = new ErrorHandler(DEFAULT_EXCEPTION_HANDLER);
    }

    @Override
    public Executor executor() {
        return queue;
    }

    @Override
    public EventSubscription subscribe(EventSubscriber<T> subscriber, Executor executor) {
        EventBusSubscription<T> subscription = new EventBusSubscription<>(
                subscriber,
                executor,
                this,
                errorHandler.forkChild());
        queue.execute(() -> subscribe(subscription));
        return subscription;
    }

    @Override
    public <E extends Throwable> EventSource<T> error(Class<E> type, Consumer<E> consumer) {
        errorHandler.error(type, consumer);
        return this;
    }

    @Override
    public void emit(T event) {
        queue.execute(() -> subscriptions.forEach(s -> s.emit(event)));
    }

    @Override
    public void error(Throwable t) {
        errorHandler.error(t);
    }

    @Override
    public boolean hasSubscribers() {
        return subscriptions.size() > 0;
    }

    private void subscribe(EventBusSubscription<T> subscription) {
        subscriptions.add(subscription);
    }

    void unsubscribe(EventSubscription subscription) {
        queue.execute(() -> subscriptions.remove(subscription));
    }
}
