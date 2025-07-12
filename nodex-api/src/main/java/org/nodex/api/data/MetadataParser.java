package org.nodex.api.data;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Parser for converting between BDF objects and metadata.
 */
@NotNullByDefault
public interface MetadataParser {
    
    /**
     * Parses a BDF dictionary into metadata.
     */
    org.nodex.api.db.Metadata parse(BdfDictionary dictionary);
    
    /**
     * Converts metadata to a BDF dictionary.
     */
    BdfDictionary toDict(org.nodex.api.db.Metadata metadata);
    
    /**
     * Merges two metadata objects.
     */
    org.nodex.api.db.Metadata merge(org.nodex.api.db.Metadata base, org.nodex.api.db.Metadata overlay);
}
