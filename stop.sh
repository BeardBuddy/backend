#!/usr/bin/env bash
# Stop the BeardBuddy backend.
#
# Kills both the Maven wrapper and the forked java process it spawns — the forked
# child is what actually holds port 8080, and it can outlive the wrapper.
set -uo pipefail

PORT="${PORT:-8080}"
stopped=0

if pkill -f 'spring-boot:run' 2>/dev/null; then
  stopped=1
fi

if pkill -f 'BeardBuddyApplication' 2>/dev/null; then
  stopped=1
fi

sleep 1

# Anything still holding the port (e.g. an orphaned fork) goes too.
pids=$(lsof -nP -tiTCP:"$PORT" -sTCP:LISTEN 2>/dev/null || true)
if [[ -n "$pids" ]]; then
  echo "Clearing port $PORT (pids: $pids)"
  kill $pids 2>/dev/null || true
  sleep 2
  pids=$(lsof -nP -tiTCP:"$PORT" -sTCP:LISTEN 2>/dev/null || true)
  [[ -n "$pids" ]] && kill -9 $pids 2>/dev/null || true
  stopped=1
fi

if [[ "$stopped" == "1" ]]; then
  echo "Backend stopped."
else
  echo "No running backend found."
fi
