#!/usr/bin/env bash
# fdroid-preflight-init.sh
#
# Starts and initializes the official F-Droid buildserver image so `fdroid`
# commands run inside it behave like the real fdroiddata CI, instead of
# `pip install fdroidserver` (which drifts from the apt-installed deps the
# official CI uses — see fdroid-release skill lessons-learned.md #38).
#
# Fixes the 3 real friction points found 2026-09-18 testing health-tracker-app
# (fdroid-release skill lessons-learned.md #40):
#   1. The image does not ship fdroidserver — must `apt-get install` it.
#   2. ANDROID_HOME is only set by /etc/profile.d/bsenv.sh, which only a
#      LOGIN shell sources — plain `docker exec` misses it.
#   3. The image has no `sudo`, which `fdroid build --on-server` needs.
#
# Usage:
#   FDROIDDATA_DIR=/path/to/fdroiddata-clone ./fdroid-preflight-init.sh start
#   ./fdroid-preflight-init.sh exec fdroid lint <appId>
#   ./fdroid-preflight-init.sh exec fdroid rewritemeta <appId>
#   ./fdroid-preflight-init.sh exec fdroid build --verbose --test <appId>
#   ./fdroid-preflight-init.sh stop
#
# Env vars (all optional except FDROIDDATA_DIR for `start`):
#   FDROIDDATA_DIR         host path to a fdroiddata clone, mounted at /fdroiddata
#                          inside the container. Must be a full (non-shallow) clone
#                          unless ALLOW_SHALLOW_FDROIDDATA=1 (see below).
#   ALLOW_SHALLOW_FDROIDDATA  set to 1 to accept a shallow FDROIDDATA_DIR clone.
#                          Safe for read-only preflight runs (no push happens);
#                          keep unset/0 if this run will push to an fdroiddata fork
#                          afterwards (lessons-learned.md #33).
#   IMAGE                  default: registry.gitlab.com/fdroid/fdroidserver:buildserver-trixie
#   CONTAINER_NAME         default: fdroid-preflight
#
# This script only sets up the container and gives you a wrapper to run
# commands inside it correctly. It does not itself decide pass/fail — check
# the exit code and output of each `exec` call yourself, and record results
# in references/preflight-evidence-template.md.

set -euo pipefail

IMAGE="${IMAGE:-registry.gitlab.com/fdroid/fdroidserver:buildserver-trixie}"
CONTAINER_NAME="${CONTAINER_NAME:-fdroid-preflight}"

log() { printf '[fdroid-preflight-init] %s\n' "$*" >&2; }

require_docker() {
  if ! docker info >/dev/null 2>&1; then
    log "ERROR: docker is not available or not running."
    exit 1
  fi
}

cmd_start() {
  require_docker

  if [ -z "${FDROIDDATA_DIR:-}" ]; then
    log "ERROR: FDROIDDATA_DIR must point at a full (non-shallow) fdroiddata clone."
    exit 1
  fi
  if [ ! -d "$FDROIDDATA_DIR" ]; then
    log "ERROR: FDROIDDATA_DIR '$FDROIDDATA_DIR' does not exist."
    exit 1
  fi
  if [ "${ALLOW_SHALLOW_FDROIDDATA:-0}" != "1" ] && \
     [ "$(cd "$FDROIDDATA_DIR" && git rev-parse --is-shallow-repository 2>/dev/null || echo unknown)" = "true" ]; then
    log "ERROR: '$FDROIDDATA_DIR' is a shallow clone. Run: git -C '$FDROIDDATA_DIR' fetch --unshallow"
    log "(A full clone is only required if you intend to PUSH to an fdroiddata fork afterwards —"
    log " see fdroid-release skill lessons-learned.md #33. For a read-only preflight run, set"
    log " ALLOW_SHALLOW_FDROIDDATA=1 and use a shallow clone to save time/bandwidth in CI.)"
    exit 1
  fi

  if docker ps -a --format '{{.Names}}' | grep -qx "$CONTAINER_NAME"; then
    log "Container '$CONTAINER_NAME' already exists — removing it first."
    docker rm -f "$CONTAINER_NAME" >/dev/null
  fi

  log "Pulling $IMAGE ..."
  docker pull "$IMAGE"

  log "Starting container '$CONTAINER_NAME' with fdroiddata mounted at /fdroiddata ..."
  docker run -d \
    --name "$CONTAINER_NAME" \
    -v "$FDROIDDATA_DIR:/fdroiddata" \
    "$IMAGE" \
    sleep infinity

  log "Fix #1: installing fdroidserver + sudo (image ships neither) ..."
  docker exec "$CONTAINER_NAME" bash -lc \
    'apt-get update -qq && apt-get install -y -qq fdroidserver sudo'

  log "Container ready. Verifying ANDROID_HOME is visible via login shell (fix #2) ..."
  local android_home
  android_home="$(docker exec "$CONTAINER_NAME" bash -lc 'echo "$ANDROID_HOME"')"
  if [ -z "$android_home" ]; then
    log "WARNING: ANDROID_HOME is still empty even via login shell. Image layout may have changed — investigate before trusting build results."
  else
    log "ANDROID_HOME=$android_home (OK)"
  fi

  log "Done. Run commands with: $0 exec <fdroid ...>"
}

cmd_exec() {
  require_docker
  if ! docker ps --format '{{.Names}}' | grep -qx "$CONTAINER_NAME"; then
    log "ERROR: container '$CONTAINER_NAME' is not running. Run '$0 start' first."
    exit 1
  fi
  if [ "$#" -eq 0 ]; then
    log "ERROR: no command given. Usage: $0 exec <command...>"
    exit 1
  fi
  # bash -lc is fix #3: a login shell is required for ANDROID_HOME to be set
  # (see /etc/profile.d/bsenv.sh in the image), and for `fdroid build
  # --on-server` to find sudo on PATH consistently.
  docker exec -w /fdroiddata "$CONTAINER_NAME" bash -lc "$*"
}

cmd_stop() {
  require_docker
  if docker ps -a --format '{{.Names}}' | grep -qx "$CONTAINER_NAME"; then
    log "Removing container '$CONTAINER_NAME' ..."
    docker rm -f "$CONTAINER_NAME" >/dev/null
  else
    log "No container named '$CONTAINER_NAME' to remove."
  fi
}

main() {
  local action="${1:-}"
  shift || true
  case "$action" in
    start) cmd_start "$@" ;;
    exec)  cmd_exec "$@" ;;
    stop)  cmd_stop "$@" ;;
    *)
      log "Usage: $0 {start|exec <cmd...>|stop}"
      exit 1
      ;;
  esac
}

# Allow this file to be `source`d (e.g. by fdroid-preflight-run.sh) to reuse
# cmd_start/cmd_exec/cmd_stop directly, without running main() or exiting the
# sourcing shell on a bare `source`.
if [ "${BASH_SOURCE[0]}" = "${0}" ]; then
  main "$@"
fi
