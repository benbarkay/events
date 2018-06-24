package com.benbarkay.events;

import java.util.*;
import java.util.stream.Collectors;

class TypeReflection {
    private final Class type;

    TypeReflection(Class type) {
        this.type = type;
    }

    public List<Class> getAssignableTypes() {
        List<Class> parentClasses = Optional.ofNullable(type.getSuperclass())
                .map(TypeReflection::new)
                .map(TypeReflection::getAssignableTypes)
                .orElse(Collections.emptyList());

        List<Class> interfaceClasses = Arrays.stream(type.getInterfaces())
                .map(TypeReflection::new)
                .map(TypeReflection::getAssignableTypes)
                .flatMap(List::stream)
                .filter(c -> !parentClasses.contains(c))
                .collect(Collectors.toList());

        List<Class> classes = new ArrayList<>();
        classes.add(type);
        classes.addAll(interfaceClasses);
        classes.addAll(parentClasses);
        return classes;
    }

}
