package org.nodex.introduction;
import org.nodex.api.FormatException;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
enum MessageType {
	REQUEST(0), ACCEPT(1), DECLINE(2), AUTH(3), ACTIVATE(4), ABORT(5);
	private final int value;
	MessageType(int value) {
		this.value = value;
	}
	int getValue() {
		return value;
	}
	static MessageType fromValue(int value) throws FormatException {
		for (MessageType m : values()) if (m.value == value) return m;
		throw new FormatException();
	}
}
