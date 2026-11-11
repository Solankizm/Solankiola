# S1Mods teardown (source at tag `1.3`, commit e41e75a)

Reviewed: `ola.py`, `android/.../grpc/S1GrpcClient.kt`, `model/ScooterManager.kt`, `CHANGELOG.md`.
Not reviewed: the bundled pre-built APKs (`balls`, `musicmod`, `androidauto`), which ship as binaries with no source in the repo.
Note: the `1.3` tag points at the v1.2 source. The v1.3 features (Dynamic Island, Lock/Unlock/Boot, My Ola dashboard) exist **only in the APK**. Their source is not public.

## How it works
1. **Discovery.** The phone runs a hotspot and the scooter joins it. The app scans IPv6 link-local addresses (MAC → EUI-64) for TCP **50005**.
2. **Root RPC.** The scooter's head unit runs a gRPC service `wifican.WifiCanRpc`:
   - `ExecuteADBCommand(string cmd) → (int status, string output)`: runs the command **as root (uid 0), with no authentication**.
   - `GetSwVersion(Empty) → (status, string)`: returns the MoveOS version.
   - Protobuf is encoded by hand: field 1 = command (wire type 2). The reply has field 1 = status (0/1 = OK) and field 2 = output.
   - The daemon splits commands on whitespace, so S1Mods wraps them as `sh -c echo${IFS}<base64>|base64${IFS}-d|sh`.
3. **ADB unlock.** It pushes the phone's ADB public key to `/data/local/tmp`, then `mount --bind`s it over `/data/misc/adb/adbkeymoveos3_pub_prod` (or `adb_keys`). After that, `adb connect <ip>:5555` works.
4. **Install.** It installs APKs over ADB, grants overlay, accessibility and microphone permissions with `pm grant` / `settings put`, then returns to `com.olaelectric.hmiclient/.MainActivity` (the stock dashboard).

Useful identifiers:
- `com.olaelectric.hmiclient`: the stock dashboard (HMI)
- `com.ola.mfgapp`: factory diagnostics
- `com.olaelectric.vehiclerangeservice`: its shared_prefs (`com.olaelectric.dataanalyser.xml`) hold `VEHICLE_VARIANT`. **This is the most likely place to find range and battery data.**

## Findings (most serious first)
| # | Finding | Risk |
|---|---|---|
| 1 | **The scooter exposes an unauthenticated root shell on port 50005** on any Wi-Fi it joins. This is Ola's flaw, not S1Mods'. Anyone on the same network (a café hotspot, a shared phone hotspot) can run root commands. | **Critical.** Only connect the scooter to your own hotspot. Consider reporting it to Ola. |
| 2 | `setprop persist.ola.security.scootermode serv`. The `persist.` prefix survives reboots, so **the claim "100% reversible with a restart" is not fully true**: the security mode stays at `serv`. | **High.** Check with `getprop persist.ola.security.scootermode` and note the original value before you change anything. |
| 3 | The bundled APKs are binaries with no source. We can't verify what they do while running with overlay and accessibility permissions. | Medium |
| 4 | Lock, Unlock and Boot (v1.3) control the vehicle through the root channel, and their code isn't public. | Medium-High. **Solankizm will not include vehicle control.** |
| 5 | The ADB key file and bind script stay in `/data/local/tmp` until removed. | Low |

## What Solankizm does differently
- It is an **ordinary Android app** on the head unit, installed once with ADB. It needs no root at runtime and holds no permanent root channel.
- **Read-only.** It reads vehicle data and never writes vehicle state: no lock/unlock, speed limiter, BMS or regen changes.
- It never touches `persist.ola.security.*`.
- All source is in this folder. Nothing is shipped as a closed binary.

## Still unknown (needs the real scooter)
- Where live speed, SOC, range and ride mode come from: a vehicle service broadcast, a content provider, a bound AIDL service, or VHAL (`android.car`). Run `tools/probe.py` (read-only) to find out.
- Screen resolution and orientation. **Assumption: 1280×720 landscape**, configurable in `ui/Dims.kt`.
