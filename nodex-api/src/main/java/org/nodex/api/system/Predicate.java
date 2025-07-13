package org.nodex.api;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface Predicate<T> {

	boolean test(T t);
}
