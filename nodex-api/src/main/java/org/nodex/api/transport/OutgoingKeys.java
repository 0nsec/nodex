package org.nodex.api.transport;

import org.nodex.api.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class OutgoingKeys {
    
    private final byte[] tagKey;
    private final byte[] headerKey;
    private final long streamCounter;
    private final long timePeriod;
    
    public OutgoingKeys(byte[] tagKey, byte[] headerKey, long streamCounter, long timePeriod) {
        this.tagKey = tagKey;
        this.headerKey = headerKey;
        this.streamCounter = streamCounter;
        this.timePeriod = timePeriod;
    }
    
    public byte[] getTagKey() { return tagKey; }
    public byte[] getHeaderKey() { return headerKey; }
    public long getStreamCounter() { return streamCounter; }
    public long getTimePeriod() { return timePeriod; }
}
