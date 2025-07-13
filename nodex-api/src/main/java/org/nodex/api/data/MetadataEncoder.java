package org.nodex.api.data;

import org.nodex.nullsafety.NotNullByDefault;

import java.util.Map;

/**
 * Encoder for metadata.
 */
@NotNullByDefault
public interface MetadataEncoder {
    /**
     * Encode metadata to bytes.
     */
    byte[] encode(Map<String, Object> metadata);
}
