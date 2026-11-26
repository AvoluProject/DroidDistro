# Changelog

## 0.2.0

- Fixed the Kotlin compile error caused by Android `View.getDisplay()` shadowing the app's display string property. The selected output is now stored as `displayMode`.
- Added Termux RUN_COMMAND integration for real commands.
- Added the PRoot-Distro install pipeline for Debian, Ubuntu, Alpine and Arch Linux.
- Added desktop package install mappings for XFCE, LXQt, LXDE, MATE and Openbox.
- Added Termux:X11 launch flow, environment start/stop/remove actions, profile compatibility checks and status broadcasts back to DroidDistro.
- Added persistent install/activity logs and Termux permission setup guidance.
- Clarified that embedded VNC is not yet available; graphical mode in this version uses Termux:X11.
