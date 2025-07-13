package org.nodex.api.transport;

import org.nodex.api.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class IncomingKeys {
    
    private final byte[] tagKey;
    private final byte[] headerKey;
    private final long windowBase;
    private final byte[] windowBitmap;
    
    public IncomingKeys(byte[] tagKey, byte[] headerKey, long windowBase, byte[] windowBitmap) {
        this.tagKey = tagKey;
        this.headerKey = headerKey;
        this.windowBase = windowBase;
        this.windowBitmap = windowBitmap;
    }
    
    public byte[] getTagKey() { return tagKey; }
    public byte[] getHeaderKey() { return headerKey; }
    public long getWindowBase() { return windowBase; }
    public byte[] getWindowBitmap() { return windowBitmap; }
}
