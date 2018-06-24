package com.benbarkay.events;

abstract class AbstractForwardingSubscriber<T, F> implements EventSubscriber<T> {

    private final VariableStrengthReference<EventEmitter<F>> emitterRef;

    AbstractForwardingSubscriber(EventEmitter<F> emitter) {
        this(new VariableStrengthReference<>(emitter));
    }

    private AbstractForwardingSubscriber(VariableStrengthReference<EventEmitter<F>> emitterRef) {
        this.emitterRef = emitterRef;
    }

    @Override
    public final void accept(T event, EventSubscription subscription) {
        // Basically, the problem is that we don't want to keep sending events to
        // dead EventEmitters -- these are EventEmitters that have no subscribers and
        // are not referenced anywhere (thus, they cannot be subscribed to).

        // The solution to this problem is to keep a strong reference which would
        // prevent the EventEmitter from being garbage collected when there are
        // subscribers, and to release the strong reference when there are no
        // subscribers. This will achieve the following:
        // 1.   When there are subscribers, not referring to the EventEmitter will keep
        //      it from being garbage collected even if nothing refers to it.
        // 2.   If there are no subscribers, but there are references aside from
        //      our strong reference, then the EventEmitter will not be garbage collected
        //      anyway, and we can safely weaken our reference.
        // 3.   If there are no subscribers and no references, then GC can clear
        //      the object freely, at which point we will cancel this subscription.

        if (emitterRef.isGarbageCollected()) {
            // The EventEmitter has been garbage collected.
            subscription.cancel();

        } else if (emitterRef.value().hasSubscribers()) {
            emitterRef.setStrong(true);
            forward(event, emitterRef.value());
        } else {
            // EventEmitter is not garbage collected, and there are no subscribers.
            // If we're the only referents to it, then it's a dead EventEmitter --
            // We can now weaken our reference without worrying about preventing
            // subscribers from not receiving events.
            emitterRef.setStrong(false);
        }
    }

    protected abstract void forward(T event, EventEmitter<F> emitter);
}
