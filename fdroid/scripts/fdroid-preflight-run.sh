#!/usr/bin/env bash
# fdroid-preflight-run.sh
#
# Single command to run before a release: runs the full F-Droid preflight gate
# set inside the official buildserver-trixie image and reports PASS/FAIL for
# each gate, so problems surface here instead of at the official fdroiddata
# fork pipeline or a reviewer's desk.
#
# Builds on fdroid-preflight-init.sh (container setup, lessons-learned #40
# fixes) and covers the gate set from lessons-learned / preflight-evidence-
# template.md that fdroiddata's own .gitlab-ci.yml actually runs per-app:
#   rewritemeta (clean diff + idempotent) / lint / schema validation /
#   checkupdates / scanner / build
#
# It does NOT touch fdroiddata upstream, does NOT push, does NOT open an MR.
# It is read-only against your project repo and only writes inside the
# fdroiddata clone you point it at (plus the report file it produces).
#
# Usage:
#   ./fdroid-preflight-run.sh <appId> <path-to-local-metadata.yml> <fdroiddata-clone-dir> [report-dir]
#
# Example:
#   ./fdroid-preflight-run.sh com.taotao.healthtracker \
#     "/Users/zengtao/Doc/My code/personal-health-records/metadata/com.taotao.healthtracker.yml" \
#     /path/to/fdroiddata-clone \
#     "/Users/zengtao/Doc/My code/personal-health-records/.agent/evals"
#
# Requirements on <fdroiddata-clone-dir>: full (non-shallow) clone of
# https://gitlab.com/fdroid/fdroiddata, upstream up to date.
#
# Env vars (all optional):
#   IMAGE            passed through to fdroid-preflight-init.sh
#   CONTAINER_NAME   passed through to fdroid-preflight-init.sh (default: fdroid-preflight)
#   KEEP_CONTAINER   if set to 1, don't stop the container on exit (debugging)
#
# Exit code: 0 if every gate passed, 1 if any gate failed or setup failed.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

APP_ID="${1:-}"
LOCAL_METADATA="${2:-}"
FDROIDDATA_DIR="${3:-}"
REPORT_DIR="${4:-.}"

if [ -z "$APP_ID" ] || [ -z "$LOCAL_METADATA" ] || [ -z "$FDROIDDATA_DIR" ]; then
  echo "Usage: $0 <appId> <path-to-local-metadata.yml> <fdroiddata-clone-dir> [report-dir]" >&2
  exit 1
fi
if [ ! -f "$LOCAL_METADATA" ]; then
  echo "ERROR: metadata file not found: $LOCAL_METADATA" >&2
  exit 1
fi

export FDROIDDATA_DIR
export CONTAINER_NAME="${CONTAINER_NAME:-fdroid-preflight}"

# shellcheck source=fdroid-preflight-init.sh
source "$SCRIPT_DIR/fdroid-preflight-init.sh"

mkdir -p "$REPORT_DIR"
TS="$(date -u +%Y%m%dT%H%M%SZ)"
LOG_DIR="$(mktemp -d)"
REPORT_FILE="$REPORT_DIR/fdroid-preflight-${APP_ID}-${TS}.md"

GATE_NAMES=()
GATE_RESULTS=()
OVERALL_PASS=1

cleanup() {
  if [ "${KEEP_CONTAINER:-0}" != "1" ]; then
    cmd_stop || true
  fi
}
trap cleanup EXIT

run_gate() {
  local name="$1"
  local remote_cmd="$2"
  local logfile="$LOG_DIR/${name}.log"
  local rc=0

  log "Gate: $name ..."
  if cmd_exec "$remote_cmd" >"$logfile" 2>&1; then
    rc=0
  else
    rc=$?
  fi

  GATE_NAMES+=("$name")
  if [ "$rc" -eq 0 ]; then
    GATE_RESULTS+=("PASS")
    log "Gate: $name -> PASS"
  else
    GATE_RESULTS+=("FAIL")
    OVERALL_PASS=0
    log "Gate: $name -> FAIL (exit $rc) — see $logfile"
  fi
}

log "Copying local metadata into fdroiddata clone: metadata/${APP_ID}.yml"
mkdir -p "$FDROIDDATA_DIR/metadata"
cp "$LOCAL_METADATA" "$FDROIDDATA_DIR/metadata/${APP_ID}.yml"

cmd_start

run_gate "schema_validation" \
  "(dpkg -s python3-pip >/dev/null 2>&1 || (apt-get update -qq && apt-get install -y -qq python3-pip)) && pip3 install --quiet --break-system-packages check-jsonschema && check-jsonschema --schemafile schemas/metadata.json metadata/${APP_ID}.yml"

run_gate "rewritemeta_no_diff" \
  "cp metadata/${APP_ID}.yml /tmp/_orig_metadata.yml && fdroid rewritemeta ${APP_ID} && diff -u /tmp/_orig_metadata.yml metadata/${APP_ID}.yml"

run_gate "rewritemeta_idempotent" \
  "cp metadata/${APP_ID}.yml /tmp/_pass1_metadata.yml && fdroid rewritemeta ${APP_ID} && diff -u /tmp/_pass1_metadata.yml metadata/${APP_ID}.yml"

run_gate "lint" \
  "fdroid lint ${APP_ID}"

run_gate "checkupdates" \
  "fdroid checkupdates --allow-dirty ${APP_ID}"

run_gate "scanner" \
  "fdroid scanner ${APP_ID}"

run_gate "build" \
  "fdroid build --verbose --test ${APP_ID}"

{
  echo "# F-Droid preflight report: $APP_ID"
  echo
  echo "- Run at (UTC): $TS"
  echo "- Image: ${IMAGE:-registry.gitlab.com/fdroid/fdroidserver:buildserver-trixie}"
  echo "- fdroiddata clone: $FDROIDDATA_DIR"
  echo "- fdroiddata HEAD: $(cd "$FDROIDDATA_DIR" && git rev-parse HEAD 2>/dev/null || echo unknown)"
  echo
  echo "| Gate | Result |"
  echo "|---|---|"
  for i in "${!GATE_NAMES[@]}"; do
    echo "| ${GATE_NAMES[$i]} | ${GATE_RESULTS[$i]} |"
  done
  echo
  if [ "$OVERALL_PASS" -eq 1 ]; then
    echo "**Overall: PASS**"
  else
    echo "**Overall: FAIL**"
  fi
  echo
  echo "## Logs"
  echo
  for name in "${GATE_NAMES[@]}"; do
    echo "### $name"
    echo '```'
    cat "$LOG_DIR/${name}.log"
    echo '```'
    echo
  done
} > "$REPORT_FILE"

log "Report written to: $REPORT_FILE"

if [ "$OVERALL_PASS" -eq 1 ]; then
  log "OVERALL: PASS"
  exit 0
else
  log "OVERALL: FAIL — see $REPORT_FILE"
  exit 1
fi
