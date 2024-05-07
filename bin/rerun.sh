#!/bin/bash

git pull origin master

mvn clean package

# Find and kill Java process
echo "Finding and killing AWSPE service process..."
pid=$(pgrep -f "java -jar target/AWSPe-0.0.1-SNAPSHOT.jar")
if [ -z "$pid" ]; then
    echo "No AWSPE  process found."
else
    echo "Killing Java process with PID: $pid"
    kill "$pid"
fi

# Execute another command
echo "Executing another command..."
# Replace the following line with your desired command
# For example: java -jar your_jar_file.jar
echo "Starting AWSPE..."

nohup java -jar ../target/AWSPe-0.0.1-SNAPSHOT.jar &

echo "Logs are available in tail -f logs/awspe.log"
#tail -f logs/awspe.log


echo  "Find the process ID (PID) of qart.py"
PID=$(ps -ef | grep 'qart.py' | grep -v grep | awk '{print $2}')

if [ -z "$PID" ]; then
  echo "qart.py is not running."
else
  echo "Killing qart.py process (PID: $PID)..."
  kill $PID
fi

# Run qart.py again in the background

echo "Starting qart.py..."
python3 qart.py &

