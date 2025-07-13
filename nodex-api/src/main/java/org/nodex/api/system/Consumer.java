package org.nodex.api;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface Consumer<T> {

	void accept(T t);
}
