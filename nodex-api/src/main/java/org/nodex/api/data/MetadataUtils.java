package org.nodex.api.data;

import org.nodex.api.FormatException;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.Metadata;

@NotNullByDefault
public class MetadataUtils {
    
    public static Metadata encode(BdfDictionary dictionary) throws FormatException {
        // Simple encoding implementation
        return new Metadata(dictionary.toString().getBytes());
    }
    
    public static BdfDictionary parse(Metadata metadata) throws FormatException {
        // Simple parsing implementation
        BdfDictionary dict = new BdfDictionary();
        // Add basic parsing logic here
        return dict;
    }
}
