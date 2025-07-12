package org.nodex.api.privategroup;
import org.nodex.api.FormatException;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public enum Visibility {
	INVISIBLE(0),
	VISIBLE(1),
	REVEALED_BY_US(2),
	REVEALED_BY_CONTACT(3);
	private final int value;
	Visibility(int value) {
		this.value = value;
	}
	public static Visibility valueOf(int value) throws FormatException {
		for (Visibility v : values()) if (v.value == value) return v;
		throw new FormatException();
	}
	public int getInt() {
		return value;
	}
}