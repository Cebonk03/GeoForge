#!/usr/bin/env bash
# run-paper-test.sh — Self-contained Paper runtime test for CI
#
# Replaces gmitch215/TestMC action (broken on Node 24) with a direct
# Paper API v3 query + server launch. No third-party action dependency.
#
# Usage:
#   ./run-paper-test.sh <mc-version> <java-home> <plugin-jar> <timeout> [commands...]
#
# Example:
#   ./run-paper-test.sh "1.21.4" "$JAVA_HOME" "GeoForge.jar" 120 \
#     "version" "plugin list" "plugin enable GeoForge"
#
# Exit codes:
#   0 — All commands succeeded, server started and stopped cleanly
#   1 — Missing dependency (jq, curl, java)
#   2 — Paper API query failed (no stable build, HTTP error)
#   3 — Server startup timed out
#   4 — Command execution failed
#   5 — Internal error (file system, permissions)

set -euo pipefail

SCRIPT_VERSION="1.0.0"
USER_AGENT="GeoForge/1.0 (GeoForge CI; +https://github.com/Cebonk03/GeoForge)"
PAPER_API_BASE="https://fill.papermc.io/v3/projects/paper"

usage() {
    cat <<EOF
run-paper-test.sh v${SCRIPT_VERSION}

Self-contained Paper runtime test for CI pipelines.
Downloads a Paper server JAR via the Paper API v3, starts it with the
given plugin, executes commands, and stops cleanly.

USAGE:
    $0 <mc-version> <java-home> <plugin-jar> <timeout> [commands...]

ARGUMENTS:
    mc-version    Minecraft version string (e.g. "1.21.4", "26.1.2")
    java-home     Path to JDK home directory (e.g. /usr/lib/jvm/java-21)
    plugin-jar    Path to plugin JAR file
    timeout       Max seconds to wait for server startup
    commands      One or more server commands to execute after startup

EXIT CODES:
    0   All commands succeeded
    1   Missing dependency
    2   Paper API query failed
    3   Server startup timed out
    4   Command execution failed
    5   Internal error

EOF
    exit 0
}

# --- Argument parsing ---
if [[ "${1:-}" == "--help" || "${1:-}" == "-h" ]]; then
    usage
fi

if [[ $# -lt 4 ]]; then
    echo "ERROR: Missing arguments. Usage: $0 <mc-version> <java-home> <plugin-jar> <timeout> [commands...]" >&2
    exit 1
fi

MC_VERSION="$1"
JAVA_HOME_DIR="$2"
PLUGIN_JAR="$3"
TIMEOUT_SECS="$4"
shift 4
COMMANDS=("$@")

# --- Dependency checks ---
for cmd in jq curl; do
    if ! command -v "$cmd" &>/dev/null; then
        echo "ERROR: Required command '$cmd' not found" >&2
        exit 1
    fi
done

JAVA_BIN="${JAVA_HOME_DIR}/bin/java"
if [[ ! -x "$JAVA_BIN" ]]; then
    echo "ERROR: Java not found at '$JAVA_BIN'" >&2
    exit 1
fi

# Save original directory and resolve absolute plugin path
ORIG_DIR=$(pwd)
case "$PLUGIN_JAR" in
    /*) PLUGIN_JAR_ABS="$PLUGIN_JAR" ;;
    *)  PLUGIN_JAR_ABS="${ORIG_DIR}/${PLUGIN_JAR}" ;;
esac
if [[ ! -f "$PLUGIN_JAR_ABS" ]]; then
    echo "ERROR: Plugin JAR not found at $PLUGIN_JAR_ABS" >&2
    exit 1
fi
PLUGIN_JAR="$PLUGIN_JAR_ABS"

echo "=== Paper Runtime Test ==="
echo "MC Version:  $MC_VERSION"
echo "Java:        $JAVA_BIN"
echo "Plugin:      $PLUGIN_JAR"
echo "Timeout:     ${TIMEOUT_SECS}s"
echo "Commands:    ${COMMANDS[*]:-(none)}"
echo ""

# --- Step 1: Query Paper API v3 for latest STABLE build ---
echo "[1/5] Querying Paper API v3 for version $MC_VERSION..."

API_URL="${PAPER_API_BASE}/versions/${MC_VERSION}/builds"
API_RESPONSE=$(curl -sS --max-time 15 -H "User-Agent: ${USER_AGENT}" "$API_URL" 2>&1) || {
    echo "ERROR: Failed to query Paper API: $API_RESPONSE" >&2
    exit 2
}

STABLE_BUILD=$(echo "$API_RESPONSE" | jq -r '[.[] | select(.channel == "STABLE")] | first | . // empty' 2>/dev/null)

if [[ -z "$STABLE_BUILD" ]]; then
    echo "ERROR: No stable build found for Paper $MC_VERSION" >&2
    echo "API response (first 500 chars):" >&2
    echo "$API_RESPONSE" | head -c 500 >&2
    exit 2
fi

BUILD_ID=$(echo "$STABLE_BUILD" | jq -r '.id')
DOWNLOAD_URL=$(echo "$STABLE_BUILD" | jq -r '.downloads["server:default"].url // empty')
BUILD_NUM=$(echo "$STABLE_BUILD" | jq -r '.downloads["server:default"].name // "paper-\(.id).jar"')

if [[ -z "$DOWNLOAD_URL" ]]; then
    echo "ERROR: No download URL for Paper $MC_VERSION build $BUILD_ID" >&2
    exit 2
fi

echo "  Build:   $BUILD_ID (stable)"
echo "  URL:     $DOWNLOAD_URL"

# --- Step 2: Setup temp server directory ---
echo "[2/5] Setting up server directory..."

SERVER_DIR=$(mktemp -d -t paper-XXXXXXXX)
trap 'rm -rf "$SERVER_DIR"' EXIT
cd "$SERVER_DIR"

PAPER_JAR="paper.jar"

echo "  Downloading Paper $MC_VERSION build $BUILD_ID..."
curl -sS --max-time 60 -H "User-Agent: ${USER_AGENT}" -o "$PAPER_JAR" "$DOWNLOAD_URL" 2>&1 || {
    echo "ERROR: Failed to download Paper server JAR" >&2
    exit 2
}

echo "  Verifying download..."
if [[ ! -f "$PAPER_JAR" || ! -s "$PAPER_JAR" ]]; then
    echo "ERROR: Downloaded Paper JAR is empty or missing" >&2
    exit 2
fi
echo "  Paper JAR: $(du -h "$PAPER_JAR" | cut -f1)"

# Accept EULA
echo "eula=true" > eula.txt

# Copy plugin
mkdir -p plugins
cp "$PLUGIN_JAR" plugins/
echo "  Plugin:   $(basename "$PLUGIN_JAR") ($(du -h "$PLUGIN_JAR" | cut -f1))"

# --- Step 3: Start server ---
echo "[3/5] Starting Paper server..."

# Create a named pipe for sending commands
FIFO_PATH=$(mktemp -u -t paper-fifo-XXXXXXXX)
mkfifo "$FIFO_PATH"

# Start server in background with stdin from the named pipe
"$JAVA_BIN" -Xmx1G -Xms1G -jar "$PAPER_JAR" nogui < "$FIFO_PATH" &
SERVER_PID=$!

# Open the named pipe for writing on the other end
exec 3>"$FIFO_PATH"

START_TIME=$SECONDS
STARTUP_OK=false

echo "  Waiting for server startup (timeout: ${TIMEOUT_SECS}s)..."
while true; do
    ELAPSED=$((SECONDS - START_TIME))
    if [[ $ELAPSED -ge $TIMEOUT_SECS ]]; then
        break
    fi

    if [[ -f logs/latest.log ]]; then
        if grep -q "Done" logs/latest.log 2>/dev/null; then
            STARTUP_OK=true
            echo "  Server started in ${ELAPSED}s"
            break
        fi
    fi

    # Check if server process is still alive
    if ! kill -0 "$SERVER_PID" 2>/dev/null; then
        echo "ERROR: Server process died during startup" >&2
        wait "$SERVER_PID" 2>/dev/null || true
        if [[ -f logs/latest.log ]]; then
            tail -20 logs/latest.log >&2
        fi
        exit 3
    fi

    sleep 2
done

if [[ "$STARTUP_OK" != true ]]; then
    echo "ERROR: Server failed to start within ${TIMEOUT_SECS}s" >&2
    if [[ -f logs/latest.log ]]; then
        echo "--- Last 20 lines of server log ---" >&2
        tail -20 logs/latest.log >&2
    fi
    kill "$SERVER_PID" 2>/dev/null || true
    exit 3
fi

# --- Step 4: Execute commands ---
echo "[4/5] Executing ${#COMMANDS[@]} command(s)..."
COMMAND_FAILED=false

for cmd in "${COMMANDS[@]}"; do
    echo "  > $cmd"
    echo "$cmd" >&3
    # Give the server time to process and log the response
    sleep 3

    # Check server is still alive after command
    if ! kill -0 "$SERVER_PID" 2>/dev/null; then
        echo "ERROR: Server died after command '$cmd'" >&2
        if [[ -f logs/latest.log ]]; then
            tail -10 logs/latest.log >&2
        fi
        COMMAND_FAILED=true
        break
    fi

    # Check for plugin enable failure in logs
    if [[ "$cmd" == "plugin enable "* ]] && [[ -f logs/latest.log ]]; then
        if tail -10 logs/latest.log | grep -qi "error\|exception\|failed\|null"; then
            echo "WARNING: Possible error after plugin enable" >&2
        fi
    fi
done

# --- Step 5: Stop server ---
echo "[5/5] Stopping server..."
echo "stop" >&3

# Wait for server to shut down gracefully
STOP_TIMEOUT=30
STOP_START=$SECONDS
while true; do
    if ! kill -0 "$SERVER_PID" 2>/dev/null; then
        break
    fi
    ELAPSED=$((SECONDS - STOP_START))
    if [[ $ELAPSED -ge $STOP_TIMEOUT ]]; then
        echo "WARNING: Server did not stop gracefully, killing..." >&2
        kill -9 "$SERVER_PID" 2>/dev/null || true
        break
    fi
    sleep 1
done

wait "$SERVER_PID" 2>/dev/null || true
exec 3>&-  # Close the pipe

echo ""
echo "=== Summary ==="
echo "  Server:    Paper $MC_VERSION (build $BUILD_ID)"
echo "  Commands:  ${#COMMANDS[@]} executed"
echo "  Status:    $([[ "$COMMAND_FAILED" != true ]] && echo "PASS" || echo "FAIL")"

# Print final log tail for debugging
if [[ -f logs/latest.log ]]; then
    echo ""
    echo "--- Last 5 server log lines ---"
    tail -5 logs/latest.log
fi

if [[ "$COMMAND_FAILED" == true ]]; then
    exit 4
fi

exit 0
