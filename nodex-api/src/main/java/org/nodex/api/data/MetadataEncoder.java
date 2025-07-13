package org.nodex.api.data;

import org.nodex.api.FormatException;
import org.nodex.api.db.Metadata;
import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface MetadataEncoder {

	Metadata encode(BdfDictionary d) throws FormatException;
}
