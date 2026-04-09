#!/bin/zsh

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
FRONTEND_DIR="$ROOT_DIR/frontend"
BACKEND_PID=""

cleanup() {
  if [[ -n "${BACKEND_PID}" ]] && kill -0 "${BACKEND_PID}" 2>/dev/null; then
    echo
    echo "Stopping backend (${BACKEND_PID})..."
    kill "${BACKEND_PID}" 2>/dev/null || true
    wait "${BACKEND_PID}" 2>/dev/null || true
  fi
}

trap cleanup EXIT INT TERM

if [[ ! -d "$BACKEND_DIR" || ! -d "$FRONTEND_DIR" ]]; then
  echo "Expected backend/ and frontend/ directories under $ROOT_DIR"
  exit 1
fi

if ! command -v npm >/dev/null 2>&1; then
  echo "npm is required but was not found in PATH."
  exit 1
fi

if [[ ! -x "$BACKEND_DIR/gradlew" ]]; then
  chmod +x "$BACKEND_DIR/gradlew"
fi

if [[ ! -d "$FRONTEND_DIR/node_modules" ]]; then
  echo "frontend/node_modules is missing. Run: cd \"$FRONTEND_DIR\" && npm install"
  exit 1
fi

echo "Starting backend on Spring Boot..."
(
  cd "$BACKEND_DIR"
  ./gradlew bootRun
) &
BACKEND_PID=$!

echo "Starting frontend on Vite..."
cd "$FRONTEND_DIR"
npm run dev
