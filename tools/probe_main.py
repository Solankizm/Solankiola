def main():
    ap = argparse.ArgumentParser(
        description="Read-only probe of an Ola S1 head unit (MoveOS port 50005).")
    ap.add_argument("--host", help="scooter IP, e.g. 192.168.43.12  (same one the app's Scan finds)")
    ap.add_argument("--ipv6", help="IPv6 link-local form, e.g. fe80::1%%6  (use --host for normal IPv4)")
    a = ap.parse_args()

    raw = a.host or a.ipv6
    if not raw:
        raise SystemExit("Give the scooter address:  --host 192.168.43.12")

    raw = raw.strip().strip("[]")
    if ":" in raw and raw.count(":") >= 2:          # IPv6
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
            "  - the address is the one the app's Connect/Scan shows")

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
