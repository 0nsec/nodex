package org.nodex.api.introduction;
import org.nodex.api.FormatException;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public enum Role {
	INTRODUCER(0), INTRODUCEE(1);
	private final int value;
	Role(int value) {
		this.value = value;
	}
	public int getValue() {
		return value;
	}
	public static Role fromValue(int value) throws FormatException {
		for (Role r : values()) if (r.value == value) return r;
		throw new FormatException();
	}
}