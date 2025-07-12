package org.nodex.api.sync;

import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import java.nio.charset.StandardCharsets;

/**
 * Type-safe wrapper for a namespaced string that uniquely identifies a sync
 * client.
 */
@Immutable
@NotNullByDefault
public class ClientId implements Comparable<ClientId> {

	/**
	 * The maximum length of a client identifier in UTF-8 bytes.
	 */
	public static final int MAX_CLIENT_ID_LENGTH = 100;

	private final String id;

	public ClientId(String id) {
		if (id == null) throw new IllegalArgumentException("Client ID cannot be null");
		byte[] bytes = id.getBytes(StandardCharsets.UTF_8);
		if (bytes.length == 0 || bytes.length > MAX_CLIENT_ID_LENGTH) {
			throw new IllegalArgumentException("Client ID length must be between 1 and " + MAX_CLIENT_ID_LENGTH + " bytes");
		}
		this.id = id;
	}

	public String getString() {
		return id;
	}

	@Override
	public int compareTo(ClientId clientId) {
		return id.compareTo(clientId.getString());
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof ClientId && id.equals(((ClientId) o).id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return id;
	}
}
