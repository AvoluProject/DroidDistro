# DroidDistro

DroidDistro is an Android 10+ front-end for creating and managing rootless Linux userlands hosted by Termux.

## Current version: 0.2.0

### Implemented

- Native Android setup wizard and device inspection.
- Distribution selection: Debian, Ubuntu, Alpine Linux and Arch Linux.
- Desktop selection: XFCE, LXQt, LXDE, MATE, Openbox or terminal-only.
- Local profile settings and dashboard.
- Real PRoot-Distro installation delegated to Termux through the Termux `RUN_COMMAND` API.
- Host-side desktop package installation commands for the selected distro/desktop.
- Termux:X11 launch path for graphical sessions (the Android X11 app must be installed separately).
- Start shell/desktop, stop active PRoot sessions, remove environment, and receive status events back in DroidDistro.
- Installation/event log stored locally.
- GitHub Actions debug APK build.

### Not implemented yet

- An embedded VNC/RFB viewer rendered inside DroidDistro. Select Termux:X11 for a working graphical display in this version.
- Multiple independent profiles / multiple installed distro instances.
- Backup and restore UI.
- Live stdout streaming from every Termux command. Status updates are sent to the app; full command output remains visible in the Termux session.
- Native Android boot or kernel replacement. PRoot runs a Linux userland on the Android kernel; this is not dual boot or a virtual machine.

## Requirements on the Android device

1. Android 10 or newer.
2. Termux installed and opened once. Use a current Termux build from an official source.
3. In Termux, enable external app commands by adding this line to `~/.termux/termux.properties`:

   ```properties
   allow-external-apps = true
   ```

   Then run `termux-reload-settings`.
4. In Android Settings, open **DroidDistro > App info > Permissions > Additional permissions** and grant **Run commands in Termux environment** (label may vary by Android/Termux version).
5. For graphical desktop sessions, install the Termux:X11 Android APK. DroidDistro installs the Termux companion package from the X11 repository during setup.

The Termux RUN_COMMAND API is documented at:
https://github.com/termux/termux-app/wiki/RUN_COMMAND-Intent

Termux:X11 setup and PRoot notes:
https://github.com/termux/termux-x11

PRoot-Distro documentation:
https://github.com/termux/proot-distro

## Install flow

1. Open DroidDistro and complete the wizard.
2. Choose Debian or Ubuntu + XFCE for a conservative first graphical profile.
3. Choose Termux:X11 as display backend.
4. Open **Check Termux connection** and complete the permission/property setup above.
5. Press **Install Linux environment**. Termux opens a session and performs the installation. Keep Termux open until it completes.
6. Install/open the Termux:X11 Android app if it is not already installed.
7. Return to DroidDistro and press **Power on Linux desktop**.

The initial download and desktop package installation can be large. Use a stable connection and ensure adequate free storage and battery.

## Build configuration

- Android Gradle Plugin: 8.13.2
- Gradle: 8.13
- JDK: 17
- Android compile SDK: 36
- Android target SDK: 35
- Android minimum SDK: 29 (Android 10)
- Package: `com.droiddistro.app`

GitHub Actions builds a debug APK on pushes to `main` and on manual workflow dispatch. Download the `DroidDistro-debug` artifact from the workflow run.
