# Changelog

## 0.3.0 — Major feature update

- Raised Android app version to 0.3.0 / versionCode 3.
- Added system-auto language preference plus Bahasa Indonesia and English selection; localized common navigation, wizard, labels, and actions.
- Added dark/light appearance setting.
- Added quick navigation for Home, Tools, Packages, and Settings.
- Expanded device inspection with kernel, screen resolution, battery state, storage usage, and app version.
- Added storage usage meters and RAM-aware first-desktop guidance.
- Added System Tools page with Termux/X11 launch shortcuts, RUN_COMMAND state, local setup health check, copyable Termux configuration, diagnostics sharing, and documentation links.
- Added Package Manager page with validated package-name input, install/remove, installed-package listing, disk usage checks, package-index refresh, system upgrade, and package-cache cleanup for Debian/Ubuntu, Alpine, and Arch.
- Added separate maintenance task states so a failed package task does not falsely mark the installed Linux environment as removed.
- Added share/export actions for activity logs and profile metadata. Explicitly labels profile export as metadata-only, not a filesystem backup.
- Added clear-log confirmation and improved page back navigation.
- Updated README with setup instructions, feature coverage, and current technical limitations.

## 0.2.0

- Fixed the Kotlin compile error caused by Android `View.getDisplay()` shadowing the app's display string property. The selected output is now stored as `displayMode`.
- Added Termux RUN_COMMAND integration for real commands.
- Added the PRoot-Distro install pipeline for Debian, Ubuntu, Alpine and Arch Linux.
- Added desktop package install mappings for XFCE, LXQt, LXDE, MATE and Openbox.
- Added Termux:X11 launch flow, environment start/stop/remove actions, profile compatibility checks and status broadcasts back to DroidDistro.
- Added persistent install/activity logs and Termux permission setup guidance.
- Clarified that embedded VNC is not yet available; graphical mode uses Termux:X11.
