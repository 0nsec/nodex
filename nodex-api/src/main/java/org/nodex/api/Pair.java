package org.nodex.api;

import org.nodex.api.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;

/**
 * A generic pair of objects.
 */
@Immutable
@NotNullByDefault
public class Pair<A, B> {
    
    private final A first;
    private final B second;
    
    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }
    
    public A getFirst() {
        return first;
    }
    
    public B getSecond() {
        return second;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return first.equals(pair.first) && second.equals(pair.second);
    }
    
    @Override
    public int hashCode() {
        return first.hashCode() * 31 + second.hashCode();
    }
}
