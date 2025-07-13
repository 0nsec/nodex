package org.nodex.api;

import org.nodex.api.nullsafety.NotNullByDefault;
import java.lang.ref.WeakReference;

@NotNullByDefault
public class WeakSingletonProvider<T> {
    
    private WeakReference<T> instanceRef;
    private final Provider<T> provider;
    
    public WeakSingletonProvider(Provider<T> provider) {
        this.provider = provider;
    }
    
    public T get() {
        T instance = instanceRef != null ? instanceRef.get() : null;
        if (instance == null) {
            instance = provider.get();
            instanceRef = new WeakReference<>(instance);
        }
        return instance;
    }
    
    @FunctionalInterface
    public interface Provider<T> {
        T get();
    }
}
