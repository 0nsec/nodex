package org.nodex.api;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Functional interface for testing objects.
 */
@NotNullByDefault
@FunctionalInterface
public interface Predicate<T> {
    
    /**
     * Test the given object.
     */
    boolean test(T object);
}
