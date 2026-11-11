#!/usr/bin/env bash
# One-line read-only probe of your Ola S1 head unit.
#
#   bash tools/probe.sh 192.168.43.12
#
# Use the SAME IP the Solankizm app's Connect/Scan shows. Your Mac and the
# scooter must be on the same network (your phone hotspot). Saves the result
# to probe-report.txt — send me that file.
set -euo pipefail
cd "$(dirname "$0")/.."

if [ $# -lt 1 ]; then
  echo "usage: bash tools/probe.sh <scooter-ip>     (e.g. 192.168.43.12)"
  exit 1
fi

# Make sure grpcio is available in a local venv (no system changes).
if [ ! -x .venv/bin/python ]; then
  python3 -m venv .venv
fi
if ! .venv/bin/python -c "import grpc" >/dev/null 2>&1; then
  echo "Installing grpcio (one time)…"
  .venv/bin/pip install -q grpcio
fi

.venv/bin/python tools/probe.py --host "$1" | tee probe-report.txt
echo
echo "Saved to: $(pwd)/probe-report.txt  — send me that file."
