#!/bin/bash

git pull origin master

mvn clean package

# Find and kill Java process
echo "Finding and killing AWSPE service process..."
pid=$(pgrep -f "com.example.s2s.voipgateway")
if [ -z "$pid" ]; then
    echo "No sip  process found."
else
    echo "Killing Java process with PID: $pid"
    kill "$pid"
fi

# Execute another command
echo "Executing another command..."
# Replace the following line with your desired command
# For example: java -jar your_jar_file.jar
# echo "Starting S2s..."

set -e

exec:java -Dexec.mainClass=com.example.s2s.voipgateway.NovaSonicVoipGateway
