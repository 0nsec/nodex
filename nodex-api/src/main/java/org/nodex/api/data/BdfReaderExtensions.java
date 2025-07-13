package org.nodex.api.data;

import org.nodex.api.FormatException;

import java.io.IOException;

public class BdfReaderExtensions {
    
    public static boolean eof(BdfReader reader) {
        try {
            // Check if reader has more data by attempting to peek
            return reader.getRemaining() == 0;
        } catch (Exception e) {
            return true;
        }
    }
    
    public static BdfList readList(BdfReader reader) throws IOException, FormatException {
        java.util.List<Object> objects = reader.readList();
        BdfList bdfList = new BdfList();
        bdfList.addAll(objects);
        return bdfList;
    }
}
