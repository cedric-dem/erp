#!/usr/bin/env bash
set -euo pipefail

MODE="${1:-}"

if [[ -z "$MODE" ]]; then
  echo "Usage: ./scripts/reset-db.sh [empty|mock]"
  exit 1
fi

case "$MODE" in
  empty|mock)
    ;;
  *)
    echo "Invalid mode '$MODE'. Use 'empty' or 'mock'."
    exit 1
    ;;
esac

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
PROJECT_ROOT="$(cd -- "$SCRIPT_DIR/.." >/dev/null 2>&1 && pwd)"

cd "$PROJECT_ROOT"

./gradlew bootRun --args="--app.db-reset.mode=$MODE --spring.main.web-application-type=none"