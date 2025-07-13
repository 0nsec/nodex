#!/bin/bash

echo "=== FINAL API COMPILATION FIXES ==="

NODEX_API="/workspaces/nodex/nodex-api/src/main/java"

echo "1. Creating missing plugin factory directories and classes..."

mkdir -p "$NODEX_API/org/nodex/api/plugin/duplex"
cat > "$NODEX_API/org/nodex/api/plugin/duplex/DuplexPluginFactory.java" << 'EOF'
package org.nodex.api.plugin.duplex;

import org.nodex.api.plugin.Plugin;
import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface DuplexPluginFactory {
    Plugin createPlugin();
    String getId();
}
EOF

mkdir -p "$NODEX_API/org/nodex/api/plugin/simplex"
cat > "$NODEX_API/org/nodex/api/plugin/simplex/SimplexPluginFactory.java" << 'EOF'
package org.nodex.api.plugin.simplex;

import org.nodex.api.plugin.Plugin;
import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface SimplexPluginFactory {
    Plugin createPlugin();
    String getId();
}
EOF

echo "2. Creating missing transport classes..."

cat > "$NODEX_API/org/nodex/api/transport/TransportProperties.java" << 'EOF'
package org.nodex.api.transport;

import org.nodex.api.nullsafety.NotNullByDefault;
import java.util.Map;

@NotNullByDefault
public interface TransportProperties {
    
    Map<String, String> getProperties();
    String getProperty(String key);
    void setProperty(String key, String value);
}
EOF

echo "3. Updating IncomingKeys and OutgoingKeys with missing methods..."

cat > "$NODEX_API/org/nodex/api/transport/IncomingKeys.java" << 'EOF'
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
    private final long timePeriod;
    
    public IncomingKeys(byte[] tagKey, byte[] headerKey, long windowBase, byte[] windowBitmap, long timePeriod) {
        this.tagKey = tagKey;
        this.headerKey = headerKey;
        this.windowBase = windowBase;
        this.windowBitmap = windowBitmap;
        this.timePeriod = timePeriod;
    }
    
    public byte[] getTagKey() { return tagKey; }
    public byte[] getHeaderKey() { return headerKey; }
    public long getWindowBase() { return windowBase; }
    public byte[] getWindowBitmap() { return windowBitmap; }
    public long getTimePeriod() { return timePeriod; }
}
EOF

cat > "$NODEX_API/org/nodex/api/transport/OutgoingKeys.java" << 'EOF'
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
EOF

echo "4. Creating ConnectionHandler..."

cat > "$NODEX_API/org/nodex/api/transport/ConnectionHandler.java" << 'EOF'
package org.nodex.api.transport;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface ConnectionHandler {
    void handleConnection();
    void closeConnection();
}
EOF

echo "5. Compiling API..."
cd /workspaces/nodex
./gradlew :nodex-api:compileJava

if [ $? -eq 0 ]; then
    echo "✓ API compilation successful!"
    
    echo "6. Now checking core compilation errors..."
    ./gradlew :nodex-core:compileJava 2>&1 | grep -E "([0-9]+ errors?|total)" | tail -2
    
    echo ""
    echo "=== CHECKING OVERALL PROJECT COMPILATION ==="
    ./gradlew compileJava 2>&1 | grep -E "(BUILD|errors?|total)" | tail -10
else
    echo "✗ API compilation still has issues"
fi

echo ""
echo "=== FINAL FIXES COMPLETE ==="
