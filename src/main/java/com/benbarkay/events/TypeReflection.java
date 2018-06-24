package com.benbarkay.events;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Reflective operations on Java types.
 */
class TypeReflection {
    private final Class type;

    /**
     * Creates a new instance for the specified type.
     * @param type  The type to create a new {@code TypeReflection} for.
     */
    TypeReflection(Class type) {
        this.type = type;
    }

    /**
     * Returns a list of assignable types for the type of this {@code TypeReflection}.
     * The returned list is ordered by the most specific to the least specific.
     */
    List<Class> getAssignableTypes() {
        List<Class> parentClasses = Optional.ofNullable(type.getSuperclass())
                .map(TypeReflection::new)
                .map(TypeReflection::getAssignableTypes)
                .orElse(Collections.emptyList());

        List<Class> interfaceClasses = Arrays.stream(type.getInterfaces())
                .map(TypeReflection::new)
                .map(TypeReflection::getAssignableTypes)
                .flatMap(List::stream)
                .distinct()
                .filter(c -> !parentClasses.contains(c))
                .collect(Collectors.toList());

        List<Class> classes = new ArrayList<>();
        classes.add(type);
        classes.addAll(interfaceClasses);
        classes.addAll(parentClasses);
        return classes;
    }

}
