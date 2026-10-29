#!/usr/bin/env bash
# Pack this atom into dist/rebuild/ — the directory tools/publish-atom.sh expects.
set -euo pipefail
cd "$(dirname "$0")"

./tools/check.sh

rm -rf dist/rebuild
mkdir -p dist/rebuild
cp task.json ui.json dist/rebuild/
echo "built dist/rebuild: $(ls dist/rebuild | tr '\n' ' ')"
