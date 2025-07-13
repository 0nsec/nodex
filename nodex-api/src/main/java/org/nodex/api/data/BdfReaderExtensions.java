package org.nodex.api.data;

import org.nodex.api.FormatException;

import java.io.IOException;

public class BdfReaderExtensions {
    
    // Since BdfReader doesn't have hasNext or getRemaining, we'll provide eof check differently
    public static boolean eof(BdfReader reader) {
        // For now, return false as we can't reliably check EOF without modifying BdfReader interface
        // This method will be enhanced when we have access to the underlying stream
        return false;
    }
    
    public static BdfList readListAsBdfList(BdfReader reader) throws IOException, FormatException {
        java.util.List<Object> objects = reader.readList();
        BdfList bdfList = new BdfList();
        bdfList.addAll(objects);
        return bdfList;
    }
}
