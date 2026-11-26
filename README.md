# DroidDistro

DroidDistro is an Android app prototype for building and launching Linux userland environments from a guided UI.

## Current status: v0.1.0 (app foundation)

Implemented:
- Android-native Kotlin app shell
- Guided setup pages for device inspection, distro, desktop and display choices
- Local profile saving
- Dashboard and clearly labelled boot-sequence preview
- GitHub Actions workflow that builds a debug APK

Not implemented yet:
- PRoot installation or distro extraction
- Real Linux process/session management
- Live boot logs
- Embedded VNC viewer or Termux:X11 integration

The boot screen in this version is only a preview. It does not pretend to boot Linux.

## Build configuration

- Android Gradle Plugin: 8.13.2
- Gradle: 8.13
- JDK: 17
- Android compile SDK: 36
- Android target SDK: 35
- Android minimum SDK: 29 (Android 10)

A GitHub Actions workflow builds the debug APK on pushes to `main` and manual workflow dispatches. It installs the Android SDK packages and publishes the APK as an Actions artifact named `DroidDistro-debug`.

## Package

`com.droiddistro.app`
