package org.nodex.android.viewmodel;
import org.nodex.nullsafety.NotNullByDefault;
@NotNullByDefault
public class MutableLiveEvent<T> extends LiveEvent<T> {
	public MutableLiveEvent(T value) {
		super(value);
	}
	public MutableLiveEvent() {
		super();
	}
	public void postEvent(T value) {
		super.postValue(new ConsumableEvent<>(value));
	}
	public void setEvent(T value) {
		super.setValue(new ConsumableEvent<>(value));
	}
}