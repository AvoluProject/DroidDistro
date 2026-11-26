# DroidDistro

**DroidDistro 0.3.0** is a lightweight Android 10+ control panel for building and managing rootless Linux userlands through Termux and PRoot-Distro. The Android app is a guided frontend; the Linux root filesystem and packages are downloaded and stored by Termux.

## What's new in 0.3.0

### Interface and personalization
- Redesigned dashboard navigation with quick access to Home, Tools, Packages, and Settings.
- Dark and light appearance modes.
- Language preference: Auto (follows the Android system language), Bahasa Indonesia, or English. The main navigation, wizard labels, actions, and common interface labels are localized.
- Larger touch targets, rounded cards, clearer setup descriptions, and more visible device/storage information.
- Back navigation between wizard steps and manager pages.

### Device and setup tools
- Device report with Android/API level, CPU ABI, RAM, storage, kernel, screen resolution, battery state, and Termux/Termux:X11 detection.
- Storage usage indicator and free-space display.
- RAM-aware desktop advice for lower-memory phones.
- Termux RUN_COMMAND permission checklist and copyable Termux configuration command.
- Direct launch shortcuts for Termux and Termux:X11.
- Shareable diagnostic report containing device/app status and recent activity log.
- Local setup health check for Termux, RUN_COMMAND permission, X11 app, available storage, and selected-profile consistency.

### Linux environment management
- Guided distro selection: Debian, Ubuntu, Alpine Linux, and Arch Linux.
- Desktop selection: XFCE, LXQt, LXDE, MATE, Openbox, or terminal-only.
- Real PRoot-Distro installation delegated to Termux through its RUN_COMMAND API.
- Termux:X11 launch path for graphical sessions (the Android X11 app is installed separately).
- Start, stop, repair/reinstall, and remove the selected Linux environment.
- Profile settings export (metadata only; it does not back up the Linux filesystem).
- Persistent local event log with refresh, share/export, and clear actions.

### Package maintenance
- Install or remove up to 20 package names in one operation using the selected distro's package manager.
- List installed packages and inspect Linux filesystem disk usage.
- Refresh package indexes.
- Upgrade installed packages.
- Clean package cache / remove unused package dependencies where supported by the selected package manager.
- Maintenance tasks report completion/failure separately and do not automatically mark an already-installed Linux environment as uninstalled.

## Requirements

1. Android 10 or newer (minimum SDK 29).
2. Install Termux from a trusted official distribution source and open it once.
3. Enable external app commands in Termux by adding this line to `~/.termux/termux.properties`:

   ```properties
   allow-external-apps = true
   ```

   Then run `termux-reload-settings`.
4. Grant DroidDistro the **Run commands in Termux environment** permission in Android Settings > Apps > DroidDistro > Permissions (the exact label may vary).
5. For graphical sessions, install the Termux:X11 Android app separately. The Termux companion package is installed by DroidDistro during setup.
6. Keep sufficient free storage and use a stable network connection. Root filesystem and desktop package sizes vary by distribution and selected desktop and can require multiple gigabytes.

## First installation

1. Open DroidDistro and start the guided setup.
2. Choose a distribution. Debian is a reasonable general-purpose starting choice.
3. Choose a desktop. XFCE or Openbox is a practical starting point on many phones; Terminal only avoids desktop package downloads.
4. Choose Termux:X11 for a graphical session or Terminal only for a shell.
5. Complete the Termux configuration and RUN_COMMAND permission checks.
6. Start **Install Linux Environment** and keep the Termux session open while installation runs.
7. Install/open the Termux:X11 Android app separately if you selected a graphical desktop.
8. Return to the dashboard and start the environment.

## Package manager

Open **Package Manager & Maintenance** after the selected Linux environment is installed. Package names are accepted as simple tokens (letters, numbers, dots, plus, colon, underscore, and hyphen); command separators and shell expressions are rejected. The commands are mapped to `apt`, `apk`, or `pacman` based on the selected distribution. Review each confirmation before running a task.

## Important limitations

- **No embedded VNC/RFB viewer:** graphical sessions use the separate Termux:X11 app.
- **No native dual boot or kernel replacement:** PRoot runs a Linux userland on the existing Android kernel. It is not a virtual machine.
- **No full filesystem backup/restore:** profile export includes only selected profile and app preferences. Linux files must be backed up separately using Termux/Linux tools.
- **No multiple independent distro profiles:** one selected PRoot-Distro environment is managed at a time.
- **No live stdout viewer inside DroidDistro:** complete command output remains in the Termux session; DroidDistro receives summarized status events and keeps a local event log.
- **No guaranteed compatibility for every desktop/package combination:** desktop package availability depends on the distro repositories and architecture.

## Build configuration

- Android Gradle Plugin: 8.13.2
- Gradle: 8.13
- JDK: 17
- Android compile SDK: 36
- Android target SDK: 35
- Android minimum SDK: 29 (Android 10)
- Application ID: `com.droiddistro.app`
- Version: `0.3.0` (versionCode `3`)

GitHub Actions builds a debug APK on pushes to `main` and on manual workflow dispatch. Download the `DroidDistro-v0.3.0-debug` artifact from the workflow run.

## References

- Termux RUN_COMMAND API: https://github.com/termux/termux-app/wiki/RUN_COMMAND-Intent
- Termux:X11: https://github.com/termux/termux-x11
- PRoot-Distro: https://github.com/termux/proot-distro
