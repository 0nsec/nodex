package org.nodex.api.sync.validation;

import org.nodex.api.data.BdfDictionary;
import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public class ValidationResult {
    
    private final boolean valid;
    private final BdfDictionary metadata;
    private final String reason;
    
    public ValidationResult(boolean valid, BdfDictionary metadata) {
        this(valid, metadata, null);
    }
    
    public ValidationResult(boolean valid, BdfDictionary metadata, String reason) {
        this.valid = valid;
        this.metadata = metadata;
        this.reason = reason;
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public BdfDictionary getMetadata() {
        return metadata;
    }
    
    public String getReason() {
        return reason;
    }
}
