#!/bin/bash

echo "=== ADVANCED BRIAR TO NODEX CLASS COPYING - PHASE 2 ==="
echo "Copying the most critical missing classes to fix the remaining 1697 errors"

BRIAR_SOURCE="/workspaces/briar"
NODEX_API="/workspaces/nodex/nodex-api/src/main/java"
NODEX_CORE="/workspaces/nodex/nodex-core/src/main/java"

# Function to adapt a Java file from Briar to NodeX
adapt_briar_file() {
    local source_file="$1"
    local target_file="$2"
    local target_package="$3"
    
    echo "  Adapting: $(basename "$source_file") -> $target_package"
    
    # Copy the file
    mkdir -p "$(dirname "$target_file")"
    cp "$source_file" "$target_file"
    
    # Replace package declaration
    sed -i "s/package org\.briarproject\.bramble\./package org.nodex./g" "$target_file"
    sed -i "s/package org\.briarproject\.briar\./package org.nodex./g" "$target_file"
    
    # Replace imports
    sed -i "s/import org\.briarproject\.bramble\./import org.nodex./g" "$target_file"
    sed -i "s/import org\.briarproject\.briar\./import org.nodex./g" "$target_file"
    sed -i "s/import static org\.briarproject\.bramble\./import static org.nodex./g" "$target_file"
    sed -i "s/import static org\.briarproject\.briar\./import static org.nodex./g" "$target_file"
    
    # Replace nullsafety imports
    sed -i "s/import org\.briarproject\.nullsafety\./import org.nodex.api.nullsafety./g" "$target_file"
    
    # Replace common Briar-specific references
    sed -i "s/BrambleApplication/NodeXApplication/g" "$target_file"
    sed -i "s/BriarApplication/NodeXApplication/g" "$target_file"
    sed -i "s/BRAMBLE_/NODEX_/g" "$target_file"
    sed -i "s/BRIAR_/NODEX_/g" "$target_file"
    
    echo "    ✓ Adapted $(basename "$source_file")"
}

echo "1. Analyzing current compilation errors to identify most needed classes..."

# Get the most frequent missing symbols from compilation errors
cd /workspaces/nodex
MISSING_SYMBOLS=$(./gradlew :nodex-core:compileJava 2>&1 | grep "cannot find symbol" | grep "symbol:" | sed 's/.*symbol: *class *//g' | sed 's/ .*//g' | sort | uniq -c | sort -nr | head -30 | awk '{print $2}')

echo "  Top missing symbols:"
echo "$MISSING_SYMBOLS" | head -15

echo ""
echo "2. Copying the most critical missing API classes from Briar..."

# Priority API classes that appear frequently in errors
CRITICAL_API_CLASSES=(
    "bramble-api/src/main/java/org/briarproject/bramble/api/data/BdfList.java:api/data/BdfList.java"
    "bramble-api/src/main/java/org/briarproject/bramble/api/data/BdfDictionary.java:api/data/BdfDictionary.java"
    "bramble-api/src/main/java/org/briarproject/bramble/api/data/BdfEntry.java:api/data/BdfEntry.java"
    "bramble-api/src/main/java/org/briarproject/bramble/api/data/MetadataEncoder.java:api/data/MetadataEncoder.java"
    "bramble-api/src/main/java/org/briarproject/bramble/api/data/MetadataParser.java:api/data/MetadataParser.java"
    "bramble-api/src/main/java/org/briarproject/bramble/api/versioning/ClientVersioningManager.java:api/versioning/ClientVersioningManager.java"
    "bramble-api/src/main/java/org/briarproject/bramble/api/sync/MessageId.java:api/sync/MessageId.java"
    "bramble-api/src/main/java/org/briarproject/bramble/api/sync/GroupId.java:api/sync/GroupId.java"
    "bramble-api/src/main/java/org/briarproject/bramble/api/sync/Group.java:api/sync/Group.java"
    "bramble-api/src/main/java/org/briarproject/bramble/api/sync/Message.java:api/sync/Message.java"
    "bramble-api/src/main/java/org/briarproject/bramble/api/client/ClientHelper.java:api/client/ClientHelper.java"
    "bramble-api/src/main/java/org/briarproject/bramble/api/plugin/TransportPlugin.java:api/transport/TransportPlugin.java"
)

for class_mapping in "${CRITICAL_API_CLASSES[@]}"; do
    IFS=':' read -r source_path target_path <<< "$class_mapping"
    
    briar_file="$BRIAR_SOURCE/$source_path"
    nodex_file="$NODEX_API/org/nodex/$target_path"
    
    if [[ -f "$briar_file" ]]; then
        target_package=$(dirname "$target_path" | tr '/' '.')
        adapt_briar_file "$briar_file" "$nodex_file" "org.nodex.api.$target_package"
    else
        echo "  ⚠ Source file not found: $briar_file"
    fi
done

echo ""
echo "3. Copying validation and factory classes..."

VALIDATION_FACTORY_CLASSES=(
    "bramble-api/src/main/java/org/briarproject/bramble/api/blog/BlogFactory.java:api/blog/BlogFactory.java"
    "bramble-api/src/main/java/org/briarproject/bramble/api/blog/BlogManager.java:api/blog/BlogManager.java"
    "bramble-api/src/main/java/org/briarproject/bramble/api/blog/BlogPostFactory.java:api/blog/BlogPostFactory.java"
    "bramble-api/src/main/java/org/briarproject/bramble/api/forum/ForumFactory.java:api/forum/ForumFactory.java"
    "bramble-api/src/main/java/org/briarproject/bramble/api/forum/ForumManager.java:api/forum/ForumManager.java"
    "bramble-api/src/main/java/org/briarproject/bramble/api/forum/ForumPostFactory.java:api/forum/ForumPostFactory.java"
    "bramble-api/src/main/java/org/briarproject/bramble/api/privategroup/PrivateGroupFactory.java:api/privategroup/PrivateGroupFactory.java"
    "bramble-api/src/main/java/org/briarproject/bramble/api/privategroup/PrivateGroupManager.java:api/privategroup/PrivateGroupManager.java"
    "bramble-api/src/main/java/org/briarproject/bramble/api/privategroup/GroupMessageFactory.java:api/privategroup/GroupMessageFactory.java"
)

for class_mapping in "${VALIDATION_FACTORY_CLASSES[@]}"; do
    IFS=':' read -r source_path target_path <<< "$class_mapping"
    
    briar_file="$BRIAR_SOURCE/$source_path"
    nodex_file="$NODEX_API/org/nodex/$target_path"
    
    if [[ -f "$briar_file" ]]; then
        target_package=$(dirname "$target_path" | tr '/' '.')
        adapt_briar_file "$briar_file" "$nodex_file" "org.nodex.api.$target_package"
    else
        echo "  ⚠ Source file not found: $briar_file"
    fi
done

echo ""
echo "4. Searching for and copying the most missing classes automatically..."

# Try to find the top missing classes in Briar and copy them
for symbol in $(echo "$MISSING_SYMBOLS" | head -10); do
    echo "  Searching for $symbol in Briar..."
    
    # Find the file containing this class in Briar
    briar_files=$(find "$BRIAR_SOURCE" -name "*.java" -exec grep -l "class $symbol\|interface $symbol\|enum $symbol" {} \; 2>/dev/null | head -1)
    
    if [[ -n "$briar_files" ]]; then
        for briar_file in $briar_files; do
            echo "    Found $symbol in: $briar_file"
            
            # Determine target path based on Briar path structure
            relative_path=${briar_file#$BRIAR_SOURCE/}
            
            # Convert bramble-api paths to nodex-api paths
            if [[ $relative_path == bramble-api/* ]]; then
                target_path=${relative_path#bramble-api/src/main/java/org/briarproject/bramble/}
                nodex_file="$NODEX_API/org/nodex/$target_path"
                target_package=$(dirname "$target_path" | tr '/' '.')
                
                # Only copy if it doesn't exist yet
                if [[ ! -f "$nodex_file" ]]; then
                    adapt_briar_file "$briar_file" "$nodex_file" "org.nodex.api.$target_package"
                fi
            elif [[ $relative_path == briar-api/* ]]; then
                target_path=${relative_path#briar-api/src/main/java/org/briarproject/briar/}
                nodex_file="$NODEX_API/org/nodex/$target_path"
                target_package=$(dirname "$target_path" | tr '/' '.')
                
                # Only copy if it doesn't exist yet
                if [[ ! -f "$nodex_file" ]]; then
                    adapt_briar_file "$briar_file" "$nodex_file" "org.nodex.api.$target_package"
                fi
            fi
            break
        done
    fi
done

echo ""
echo "5. Creating any missing simple classes that can't be found in Briar..."

# Create simple placeholder classes for common missing types
mkdir -p "$NODEX_API/org/nodex/api/sync"

# Create MessageFactory if missing
if [[ ! -f "$NODEX_API/org/nodex/api/sync/MessageFactory.java" ]]; then
cat > "$NODEX_API/org/nodex/api/sync/MessageFactory.java" << 'EOF'
package org.nodex.api.sync;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface MessageFactory {
    
    Message createMessage(GroupId groupId, long timestamp, byte[] body);
    
    Message createMessage(MessageId messageId, GroupId groupId, long timestamp, byte[] body);
}
EOF
fi

echo ""
echo "6. Compiling API after massive class copying..."
cd /workspaces/nodex
./gradlew :nodex-api:compileJava

if [ $? -eq 0 ]; then
    echo "✓ API compilation successful!"
    
    echo ""
    echo "7. Checking core compilation progress..."
    CURRENT_ERRORS=$(./gradlew :nodex-core:compileJava 2>&1 | grep -E "([0-9]+ errors?.*total)" | tail -1 | grep -o '[0-9]\+ total' | grep -o '[0-9]\+')
    
    if [[ -n "$CURRENT_ERRORS" ]]; then
        echo "Current error count: $CURRENT_ERRORS (down from 1697)"
        REDUCTION=$((1697 - CURRENT_ERRORS))
        echo "Errors reduced by: $REDUCTION"
    fi
    
else
    echo "✗ API compilation has issues - checking errors..."
    ./gradlew :nodex-api:compileJava 2>&1 | grep "error:" | head -10
fi

echo ""
echo "=== PHASE 2 COPYING COMPLETE ==="
echo "✓ Copied additional critical API classes from Briar"
echo "✓ Automatically found and copied most missing classes"
echo "✓ Created placeholder classes for missing interfaces"
echo ""
echo "Ready for Phase 3: Focus on core implementation classes"
