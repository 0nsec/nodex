package org.nodex.privategroup.invitation;
import org.nodex.api.FormatException;
import org.nodex.api.sync.Group.Visibility;
import org.nodex.api.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
import static org.nodex.api.sync.Group.Visibility.INVISIBLE;
import static org.nodex.api.sync.Group.Visibility.SHARED;
@Immutable
@NotNullByDefault
enum CreatorState implements State {
	START(0, INVISIBLE),
	INVITED(1, INVISIBLE),
	JOINED(2, SHARED),
	LEFT(3, INVISIBLE),
	DISSOLVED(4, INVISIBLE),
	ERROR(5, INVISIBLE);
	private final int value;
	private final Visibility visibility;
	CreatorState(int value, Visibility visibility) {
		this.value = value;
		this.visibility = visibility;
	}
	@Override
	public int getValue() {
		return value;
	}
	@Override
	public Visibility getVisibility() {
		return visibility;
	}
	@Override
	public boolean isAwaitingResponse() {
		return this == INVITED;
	}
	static CreatorState fromValue(int value) throws FormatException {
		for (CreatorState s : values()) if (s.value == value) return s;
		throw new FormatException();
	}
}
