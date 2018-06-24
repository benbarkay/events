package com.benbarkay.events;

import java.lang.ref.WeakReference;

/**
 * A reference of variable strength. This class can be used in order to
 * vary the reference strength to an object, thus either preventing or allowing
 * garbage collection to collect the referred object.
 * @param <T>   The referenced object type.
 */
class VariableStrengthReference<T> {

    @SuppressWarnings("unused")
    private T strongRef;
    private final WeakReference<T> weakRef;

    /**
     * Constructs a new variable strength reference instance.
     * The constructed reference is strong by default.
     * @param ref   The object to refer to.
     */
    VariableStrengthReference(T ref) {
        this.strongRef = ref;
        this.weakRef = new WeakReference<>(ref);
    }

    /**
     * Whether or not the object has been garbage collected.
     */
    public boolean isGarbageCollected() {
        return weakRef.get() == null;
    }

    /**
     * Sets whether or not this reference strongly or weakly refers to its object.
     * @param strong    Whether or not this reference will be strongly or weakly referring
     *                  to the referred object.
     */
    public void setStrong(boolean strong) {
        strongRef = strong? weakRef.get() : null;
    }

    /**
     * Returns the value of this reference.
     * @return The value of this reference, or {@code null} if the reference has been
     *         garbage collected.
     */
    public T value() {
        return weakRef.get();
    }

}
