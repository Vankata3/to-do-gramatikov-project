#!/bin/bash

echo "Building Todo Java Server..."
mvn clean package -q

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo ""
echo "Starting Todo Java Server..."
echo "Open http://localhost:8080 in your browser"
echo ""
java -jar target/todo-java-1.0.0.jar
