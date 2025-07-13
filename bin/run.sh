#!/bin/bash
set -e

# Find and kill Java process
echo "Finding and killing NovaSonicVoipGateway  service process..."
pid=$(pgrep -f "com.punit.sts.NovaSonicVoipGateway")
if [ -z "$pid" ]; then
    echo "No sonic  process found."
else
    echo "Killing Java process with PID: $pid"
    kill "$pid"
fi

# Execute another command
echo "Executing another command..."
echo "Starting sonic demo..."

mvn compile exec:java -Dexec.mainClass=com.punit.sts.NovaSonicVoipGateway
tail -f nohup.out