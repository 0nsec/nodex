package org.nodex.api.data;

import org.nodex.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.util.List;

/**
 * Reader for BDF (Briar Data Format) data.
 */
@NotNullByDefault
public interface BdfReader {
    /**
     * Read a boolean value.
     */
    boolean readBoolean() throws IOException;
    
    /**
     * Read an integer value.
     */
    int readInteger() throws IOException;
    
    /**
     * Read a long value.
     */
    long readLong() throws IOException;
    
    /**
     * Read a string value.
     */
    String readString() throws IOException;
    
    /**
     * Read a list.
     */
    List<Object> readList() throws IOException;
    
    /**
     * Close the reader.
     */
    void close() throws IOException;
    
    /**
     * Check if end of file reached.
     */
    boolean eof() throws IOException;
    
    /**
     * Read a BdfList.
     */
    BdfList readBdfList() throws IOException;
}
