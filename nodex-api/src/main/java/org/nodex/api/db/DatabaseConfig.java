package org.nodex.api.db;

import org.nodex.api.nullsafety.NotNullByDefault;
import java.io.File;

@NotNullByDefault
public interface DatabaseConfig {
    
    /**
     * Get the database directory.
     */
    File getDatabaseDirectory();
    
    /**
     * Get the database key for encryption.
     */
    String getDatabaseKey();
    
    /**
     * Get maximum database size in bytes.
     */
    long getMaxSize();
}
