# Solankizm — a custom dashboard for Ola S1 / S1 Pro

An independent, **read-only** infotainment dashboard for the MoveOS head unit.
Kotlin + Jetpack Compose. Clean Ola-style UI, ride-mode accent colours, a live
speedometer, battery, range, trip and charging screens.

> Independent project, **not affiliated with Ola Electric**. "Ola" is a
> trademark of its owner. For personal use on your own scooter.

## What it is (and isn't)
- **Is:** an ordinary Android app you install once over ADB. It reads vehicle
  data and shows it nicely. No root needed at runtime.
- **Isn't:** a vehicle controller. It never locks/unlocks, never changes speed
  limit, battery, regen or firmware, never spends money, never posts anything.
  See `docs/REVERSE-ENGINEERING.md` for why that line matters.

## Folder map
```
app/                  the Android app (Gradle project)
  app/src/main/...     Kotlin source — UI, data, viewmodel
  build.sh             build the APK
  install.sh           push it to the scooter over ADB
tools/probe.py         READ-ONLY probe to find where live data lives on MoveOS
reference/s1mods/      the original S1Mods v1.3 source, for study only
docs/REVERSE-ENGINEERING.md   teardown + safety findings
preview/index.html     static UI mockup you can open in any browser
.env.sh                sets JAVA_HOME / ANDROID_HOME for this machine
```

## Build
Toolchain is already installed on this Mac (JDK 21, Android SDK 34, build-tools).
```
cd app && ./build.sh
# -> app/build/outputs/apk/debug/app-debug.apk
```

## Run without a scooter
The app ships with a **Demo** data source that simulates a ride, so the whole UI
works on an emulator or any Android device. Live data turns on once the MoveOS
source is wired (step below).

## Put it on the scooter
1. Connect the scooter to **your own phone hotspot** (never public Wi-Fi — see safety note).
2. Authorise ADB once (via S1Mods, or `adb connect <scooter-ip>:5555`).
3. `cd app && ./install.sh <scooter-ip>`

## Wire real telemetry (needs the scooter once)
Run the read-only probe and send me the output:
```
pip install grpcio
python tools/probe.py --ipv6 <scooter-ipv6>%<scope> > probe-report.txt
```
It only reads — no writes, no shell, no installs. The report shows where speed,
SOC, range and ride mode live; then `MoveOsVehicleSource.kt` gets filled in.

## Safety note (important)
Your scooter runs an **unauthenticated root service on port 50005** on any Wi-Fi
it joins — that's Ola's flaw, not this app's. Anyone on the same network can run
root commands. Only ever connect the scooter to a network you control. Worth
reporting to Ola.

## Status
- [x] Toolchain installed, project scaffolded, UI built, Demo mode
- [ ] Build APK (needs your OK to run Gradle — it downloads AGP)
- [ ] `probe-report.txt` from your scooter
- [ ] Real telemetry wired into `MoveOsVehicleSource`
- [ ] Test on your S1 Pro
