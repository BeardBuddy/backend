#!/usr/bin/env bash
# Start the BeardBuddy Java backend on http://localhost:8080
#
# The schema and seed data are recreated on every start, so this is safe to run
# repeatedly and always produces the same dataset. Pass --clean to also delete
# the SQLite file first.
set -euo pipefail

cd "$(dirname "$0")"

if [[ "${1:-}" == "--clean" ]]; then
  echo "Removing beardbuddy.db ..."
  rm -f beardbuddy.db beardbuddy.db-journal beardbuddy.db-shm beardbuddy.db-wal
fi

if ! command -v mvn >/dev/null 2>&1; then
  echo "Maven is not installed. Install it with: brew install maven" >&2
  exit 1
fi

PORT="${PORT:-8080}"
if lsof -nP -iTCP:"$PORT" -sTCP:LISTEN >/dev/null 2>&1; then
  echo "Port $PORT is already in use:" >&2
  lsof -nP -iTCP:"$PORT" -sTCP:LISTEN >&2
  echo >&2
  echo "Run ./stop.sh to free it, or start on another port: PORT=8081 ./run.sh" >&2
  exit 1
fi

echo "Starting BeardBuddy backend on http://localhost:$PORT ..."
exec mvn -B spring-boot:run -Dspring-boot.run.arguments=--server.port="$PORT"
