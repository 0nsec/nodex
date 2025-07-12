package org.nodex.privategroup.invitation;

import org.nodex.api.FormatException;
import org.nodex.api.sync.Group.Visibility;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

import static org.nodex.api.sync.Group.Visibility.INVISIBLE;
import static org.nodex.api.sync.Group.Visibility.SHARED;
import static org.nodex.api.sync.Group.Visibility.VISIBLE;
@Immutable
@NotNullByDefault
enum PeerState implements State {
	START(0, INVISIBLE),
	AWAIT_MEMBER(1, INVISIBLE),
	NEITHER_JOINED(2, INVISIBLE),
	LOCAL_JOINED(3, VISIBLE),
	BOTH_JOINED(4, SHARED),
	LOCAL_LEFT(5, INVISIBLE),
	ERROR(6, INVISIBLE);
	private final int value;
	private final Visibility visibility;
	PeerState(int value, Visibility visibility) {
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
		return false;
	}
	static PeerState fromValue(int value) throws FormatException {
		for (PeerState s : values()) if (s.value == value) return s;
		throw new FormatException();
	}
}