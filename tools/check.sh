#!/usr/bin/env bash
# Assert the two JSON files are a declaration the runner can honour. Runs in CI and from build.sh.
set -euo pipefail
cd "$(dirname "$0")/.."

python3 - <<'PY'
import json, sys

def fail(message):
    print("check failed: " + message, file=sys.stderr)
    sys.exit(1)

with open("task.json", encoding="utf-8") as f:
    task = json.load(f)
if task.get("atomCode") != "rebuild":
    fail("atomCode must be rebuild")
if not (task.get("execution") or {}).get("target"):
    fail("task.json needs execution.target (even an atom that only declares a surface)")
protocol = (task.get("x-jenkins") or {}).get("protocol", "jenkins-1")
if protocol != "jenkins-1":
    fail("protocol must be jenkins-1, got " + str(protocol))

with open("ui.json", encoding="utf-8") as f:
    ui = json.load(f)
if ui.get("schema") != 1:
    fail("ui.json schema must be 1")
surfaces = ui.get("surfaces") or []
if len(surfaces) != 1:
    fail("expected exactly one surface, got %d" % len(surfaces))

surface = surfaces[0]
if surface.get("where") != "run":
    fail("surface.where must be run")
if surface.get("id") != "rebuild":
    fail("surface.id must be rebuild")

blocks = surface.get("blocks") or []
actions = [b for b in blocks if b.get("kind") == "action"]
if len(actions) != 1:
    fail("expected exactly one action block")
action = actions[0].get("action") or {}
if action.get("kind") != "build":
    fail("action.kind must be build")
if (action.get("params") or {}).get("from") != "build":
    fail("action must copy the build's parameters (params.from=build)")

print("check ok: 1 run surface, 1 build action")
PY
