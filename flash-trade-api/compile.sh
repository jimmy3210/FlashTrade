#!/bin/bash
cd "$(dirname "$0")"
echo "Starting Maven compilation..."
mvn clean compile -q
if [ $? -eq 0 ]; then
    echo "✅ Compilation successful!"
    exit 0
else
    echo "❌ Compilation failed!"
    mvn clean compile 2>&1 | tail -50
    exit 1
fi
