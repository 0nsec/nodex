package org.nodex.api.data;

import org.nodex.nullsafety.NotNullByDefault;

import java.io.InputStream;

/**
 * Factory for creating BDF (Briar Data Format) readers.
 */
@NotNullByDefault
public interface BdfReaderFactory {
    /**
     * Create a BDF reader from an input stream.
     */
    BdfReader createReader(InputStream inputStream);
}
