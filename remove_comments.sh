#!/bin/bash
for file in $(find /workspaces/nodex -name "*.java"); do
    echo "Processing: $file"
    temp_file=$(mktemp)
    sed -E '
        s|//.*$||
        s|/\*.*\*/||g
        /\/\*/{
            :loop
            /\*\//{
                s|/\*.*\*/||g
                b
            }
            N
            b loop
        }
    ' "$file" > "$temp_file"
    sed -i '/^[[:space:]]*$/d' "$temp_file"
    sed -i 's/[[:space:]]*$//' "$temp_file"
    mv "$temp_file" "$file"
done
echo "Comment removal completed!"
