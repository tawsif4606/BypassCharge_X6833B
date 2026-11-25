# Bypass Charge (QS tile)

Quick Settings tile that toggles `/sys/devices/platform/charger/bypass_charger`
(0 = normal charging, 1 = bypass) on the Infinix Note 30 (X6833B). Needs root.
There is no launcher icon: the app only adds a tile.

## Build the APK
1. Open this folder in Android Studio (it downloads the Android SDK and Gradle itself).
2. Build > Build Bundle(s) / APK(s) > Build APK(s).
3. The APK is at `app/build/outputs/apk/debug/app-debug.apk` (debug-signed, installable).

Command line (needs the Android SDK installed and ANDROID_HOME set):
`gradle assembleDebug`

## Install and use
1. `adb install -r app-debug.apk`
2. Pull down Quick Settings, tap the pencil (edit), drag "Bypass charge" into the active tiles.
3. Tap the tile once; grant the root request in Magisk/KernelSU (choose "always").
4. The tile shows On (bypass) / Off (normal) / No root.
