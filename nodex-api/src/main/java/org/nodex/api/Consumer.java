package org.nodex.api;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Functional interface for consuming objects.
 */
@NotNullByDefault
@FunctionalInterface
public interface Consumer<T> {
    
    /**
     * Accept and process the given object.
     */
    void accept(T object);
}
