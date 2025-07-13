package org.nodex.api.sync;

import org.nodex.api.UniqueId;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Type-safe wrapper for a byte array that uniquely identifies a {@link Group}.
 */
@ThreadSafe
@NotNullByDefault
public class GroupId extends UniqueId {

	/**
	 * Label for hashing groups to calculate their identifiers.
	 */
	public static final String LABEL = "org.briarproject.bramble/GROUP_ID";

	public GroupId(byte[] id) {
		super(id);
	}
}
