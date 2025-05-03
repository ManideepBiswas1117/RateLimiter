#!/bin/bash
# wait-for-redis.sh

# Redis hostname and port
host="$1"
port="$2"
shift 2

# Wait for Redis to be ready
echo "Waiting for Redis at $host:$port to be ready..."
until nc -z -v -w30 $host $port
do
  echo "Waiting for Redis to be up..."
  sleep 1
done

echo "Redis is up and ready. Starting the application..."

# Execute the command passed to the script
exec "$@"
