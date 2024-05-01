#!/bin/bash

git pull origin master

mvn clean package

# Find and kill Java process
echo "Finding and killing Java process..."
pid=$(pgrep -f "java -jar target/AWSPe-0.0.1-SNAPSHOT.jar")
if [ -z "$pid" ]; then
    echo "No Java process found."
else
    echo "Killing Java process with PID: $pid"
    kill "$pid"
fi

# Execute another command
echo "Executing another command..."
# Replace the following line with your desired command
# For example: java -jar your_jar_file.jar
nohup java -jar target/AWSPe-0.0.1-SNAPSHOT.jar &

tail -f logs/awspe.log

