# Solankizm — a custom dashboard for Ola S1 / S1 Pro

An independent, **read-only** dashboard for the MoveOS head unit, with a
**Phone Connect** mode so you can also read your scooter live from an Android
phone. Kotlin + Jetpack Compose. Ride-mode accent colours, a live speedometer,
battery, range, trip and charging screens, and a rider display picture.

> Independent project, **not affiliated with Ola Electric**. "Ola" is a
> trademark of its owner. For personal use on your own scooter.

## What it is (and isn't)
- **Is:** an Android app. Run it on the scooter's own screen (installed over
  ADB), or on your phone and **connect to the scooter over Wi-Fi** to read real
  device info.
- **Isn't:** a vehicle controller. It never locks/unlocks, never changes speed
  limit, battery, regen or firmware, never spends money. See
  `docs/REVERSE-ENGINEERING.md` for why that line matters.

## Get the app
- **Download the APK:** GitHub → **Actions** tab → latest green run → **Artifacts
  → Solankizm-apk** (unzip for `Solankizm.apk`). Or, once a `v*` tag is pushed,
  from the **Releases** page.
- **Build locally:** `cd app && ./build.sh` → `app/build/outputs/apk/debug/app-debug.apk`

## Two ways to use it
### A. Phone Connect (easiest to try)
1. Turn on your phone **hotspot**; on the scooter, **Settings → Wi-Fi → join it**.
   Keep the scooter screen awake, side stand UP.
2. Open Solankizm on your phone → **Connect** tab (Wi-Fi icon) → **Scan**
   (or type the scooter IP from Hotspot → Connected Devices) → **Connect**.
3. You'll see real live info: **Model, MoveOS, Android, RAM, Uptime**.
   *(Live speed / battery / range come after the probe step below.)*

### B. Install on the scooter screen
1. Scooter on your hotspot; authorise ADB once (via S1Mods, or `adb connect <ip>:5555`).
2. `cd app && ./install.sh <scooter-ip>` → open **Solankizm** from the scooter app drawer.

## Your display picture
Settings → **Add photo** (system picker), or push from the Mac:
`app/set-photo.sh <scooter-ip> ~/your-photo.jpg`.

## Wire real telemetry (needs the scooter once)
Live speed, state-of-charge and range aren't shown yet because their exact
source on MoveOS must be confirmed. Run the **read-only** probe and share the
output:
```
pip install grpcio
python3 tools/probe.py --ipv6 <scooter-ipv6>%<scope> > probe-report.txt
```
It only reads — no writes, no installs. The report locates speed/SOC/range;
then `MoveOsVehicleSource.kt` + the Connect screen get the live numbers.

## Safety note (important)
Your scooter runs an **unauthenticated root service on port 50005** on any Wi-Fi
it joins — that's Ola's flaw, not this app's. Anyone on the same network can run
root commands. Only ever connect the scooter to a network you control.

## Status
- [x] UI, Demo mode, rider photo
- [x] CI builds the APK on every push (GitHub Actions)
- [x] **Phone Connect mode** — gRPC link, live device info, Scan + remembered IP
- [ ] `probe-report.txt` from the scooter → live speed / battery / range
- [ ] Tag a release (`git push origin v0.1.0`) for a Releases download page
