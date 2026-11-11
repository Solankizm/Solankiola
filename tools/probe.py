#!/usr/bin/env python3
"""Solankizm read-only probe for an Ola S1 head unit.

Finds where live vehicle data (speed, SOC, range, ride mode) lives, so the
Solankizm app can read it. It runs ONLY the fixed, read-only commands below.
There is no free-text shell and it never writes, mounts, setprops or installs.

    python3 -m pip install grpcio
    python3 tools/probe.py --host 192.168.43.12 > probe-report.txt
    # (IPv6 link-local also works:  --ipv6 fe80::xxxx%<scope>)

Use the SAME address the app's Connect/Scan shows. Your Mac and the scooter
must be on the same network (your phone hotspot). Only run this on YOUR scooter.
"""
import argparse
import base64
import sys

try:
    import grpc
except ImportError:
    sys.exit("Missing grpcio:  python3 -m pip install grpcio")

PORT = 50005

# label -> command. Every command is read-only.
PROBES = [
    ("MoveOS / Android", "getprop ro.build.display.id; getprop ro.build.version.release"),
    ("Security mode (do not change)", "getprop persist.ola.security.scootermode"),
    ("Screen size", "wm size; wm density"),
    ("Ola packages", "pm list packages | grep -iE 'ola|vehicle|car'"),
    ("Ola services/providers/receivers",
     "for p in $(pm list packages | grep -iE 'olaelectric|ola\\.' | cut -d: -f2); do "
     "echo \"== $p\"; dumpsys package $p | grep -E 'Provider|Service|Receiver|Action:|authority' | head -60; done"),
    ("Range service prefs", "ls /data/data/com.olaelectric.vehiclerangeservice/shared_prefs/ 2>/dev/null; "
     "cat /data/data/com.olaelectric.vehiclerangeservice/shared_prefs/*.xml 2>/dev/null | head -120"),
    ("Car / VHAL service", "dumpsys -l | grep -iE 'car|vehicle|ola'"),
    ("Vehicle props (VHAL)", "dumpsys android.hardware.automotive.vehicle.IVehicle/default 2>/dev/null | head -80; "
     "dumpsys car_service 2>/dev/null | head -80"),
    ("Recent Ola broadcasts", "dumpsys activity broadcasts history | grep -iE 'olaelectric|ola\\.' | sort | uniq -c | sort -rn | head -40"),
]


def _vi(n):
    out = bytearray()
    while True:
        b = n & 0x7F
        n >>= 7
        out.append(b | 0x80 if n else b)
        if not n:
            return bytes(out)


def _read_vi(buf, i):
    v = s = 0
    while i < len(buf):
        b = buf[i]; i += 1
        v |= (b & 0x7F) << s
        if not b & 0x80:
            return v, i
        s += 7
    return v, i


def _decode(buf):
    f, i = {}, 0
    while i < len(buf):
        key, i = _read_vi(buf, i)
        fld, wire = key >> 3, key & 7
        if wire == 0:
            f[fld], i = _read_vi(buf, i)
        elif wire == 2:
            ln, i = _read_vi(buf, i)
            f[fld] = buf[i:i + ln].decode("utf-8", "replace"); i += ln
        else:
            break
    return f.get(1, 0), f.get(2, "")


def run(channel, cmd):
    b64 = base64.b64encode(cmd.encode()).decode()
    wrapped = ("sh -c echo${IFS}" + b64 + "|base64${IFS}-d|sh").encode()
    payload = _vi((1 << 3) | 2) + _vi(len(wrapped)) + wrapped
    fn = channel.unary_unary("/wifican.WifiCanRpc/ExecuteADBCommand",
                             request_serializer=lambda b: b, response_deserializer=lambda b: b)
    return _decode(fn(payload, timeout=40))


def main():
    ap = argparse.ArgumentParser(
        description="Read-only probe of an Ola S1 head unit (MoveOS port 50005).")
    ap.add_argument("--host", help="scooter IP, e.g. 192.168.43.12  (same one the app's Scan finds)")
    ap.add_argument("--ipv6", help="IPv6 link-local form, e.g. fe80::1%%6  (use --host for normal IPv4)")
    a = ap.parse_args()

    raw = (a.host or a.ipv6 or "").strip().strip("[]")
    if not raw:
        raise SystemExit("Give the scooter address:  --host 192.168.43.12")

    if raw.count(":") >= 2:                         # IPv6
        target = f"ipv6:[{raw.replace('%', '%25')}]:{PORT}"
    else:                                           # IPv4 or hostname
        target = f"{raw}:{PORT}"

    ch = grpc.insecure_channel(
        target, options=[("grpc.max_receive_message_length", 16 << 20)])
    try:
        grpc.channel_ready_future(ch).result(timeout=8)
    except grpc.FutureTimeoutError:
        raise SystemExit(
            f"No answer from {raw}:{PORT}. Check:\n"
            "  - the Mac and the scooter are on the SAME network (your phone hotspot)\n"
            "  - the scooter screen is awake, side stand up\n"
            "  - the address matches what the app's Connect/Scan shows")

    print(f"# Solankizm probe  target={raw}:{PORT}")
    for label, cmd in PROBES:
        print(f"\n######## {label}\n$ {cmd}")
        try:
            _, out = run(ch, cmd)
            print(out.strip() or "(empty)")
        except grpc.RpcError as e:
            print(f"RPC error: {e.code().name}")
    ch.close()


if __name__ == "__main__":
    main()
