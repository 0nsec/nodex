package org.nodex.api.data;

import org.nodex.api.system.FormatException;
import org.nodex.api.db.Metadata;
import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface MetadataParser {

	BdfDictionary parse(Metadata m) throws FormatException;
}
